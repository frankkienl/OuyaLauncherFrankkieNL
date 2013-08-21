package nl.frankkie.ouyalauncher.web;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import nl.frankkie.ouyalauncher.MyServer;
import nl.frankkie.ouyalauncher.WebserverActivity;
import nl.frankkie.ouyalauncher.databaserows.DatabaseAppInfo;
import nl.wotuu.database.DatabaseOpenHelper;

/**
 * Created by FrankkieNL on 13-8-13.
 */
public class AppImage extends WebPage {
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response res = null;
        String packageName = parms.get("packageName");
        DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(WebserverActivity.context);
        //TODO: find better way to prevent SQL-INJECTION
        if (packageName.contains(";")) {
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "App not found !\nSQL");
        }
        Cursor cursor = helper.WriteableDatabase.rawQuery("SELECT id FROM appinfo WHERE packageName = '" + packageName + "'", null);
        if (cursor.getCount() == 0) {
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "App not found !\nGo to applist and restart webserver.");
        }
        cursor.moveToFirst();
        DatabaseAppInfo info = new DatabaseAppInfo(cursor.getInt(0));
        info.OnLoad();
        File f;
        if (info.isOUYA()) { //so it should have a thumbnail
            f = new File("/sdcard/FrankkieOuyaLauncher/thumbnails/" + packageName + ".png");
            if (!f.exists()) {
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "Image not found !");
            }
            return serveFile(f, header);
        } else {
            PackageManager packageManager = WebserverActivity.context.getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                Drawable d = packageInfo.applicationInfo.loadIcon(packageManager);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageInByte = stream.toByteArray();
//                System.out.println("........length......"+imageInByte);
//                ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
                return serveImageBytes(imageInByte, header);
            } catch (Exception e) {
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "Not a OUYA app. (Normal Android apps will not have an Image)");
            }
        }
        ////////////////////////////////

        //return res;
    }

    public NanoHTTPD.Response serveImageBytes(byte[] bytes, Map<String, String> header) {
        NanoHTTPD.Response res = null;
        String mime = null;
        mime = "image/png";
        res = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, mime, new ByteArrayInputStream(bytes));
        res.addHeader("Content-Length", "" + bytes.length);
        return res;
    }

    public NanoHTTPD.Response serveFile(File f, Map<String, String> header) {
        NanoHTTPD.Response res = null;
        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                String mime = null;
                int dot = f.getCanonicalPath().lastIndexOf('.');
                if (dot >= 0) {
                    mime = MyServer.MIME_TYPES.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
                }
                if (mime == null) {
                    mime = NanoHTTPD.MIME_DEFAULT_BINARY;
                }

                // Calculate etag
                String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f.length()).hashCode());

                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = header.get("range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                // Change return code and add Content-Range header when skipping is requested
                long fileLen = f.length();
                if (range != null && startFrom >= 0) {
                    if (startFrom >= fileLen) {
                        res = new NanoHTTPD.Response(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                        res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                        res.addHeader("ETag", etag);
                    } else {
                        if (endAt < 0) {
                            endAt = fileLen - 1;
                        }
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0) {
                            newLen = 0;
                        }

                        final long dataLen = newLen;
                        FileInputStream fis = new FileInputStream(f) {
                            @Override
                            public int available() throws IOException {
                                return (int) dataLen;
                            }
                        };
                        fis.skip(startFrom);

                        res = new NanoHTTPD.Response(NanoHTTPD.Response.Status.PARTIAL_CONTENT, mime, fis);
                        res.addHeader("Content-Length", "" + dataLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                } else {
                    if (etag.equals(header.get("if-none-match")))
                        res = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_MODIFIED, mime, "");
                    else {
                        res = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, mime, new FileInputStream(f));
                        res.addHeader("Content-Length", "" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                }
            }
        } catch (IOException ioe) {
            res = new NanoHTTPD.Response(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
        return res;
    }
}
