package nl.frankkie.ouyalauncher.web;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by FrankkieNL on 11-8-13.
 */
public class Upload extends WebPage {
    public static final String page = "<html>" +
            "<head><title>BAXY</title></head>" +
            "<body>" +
            "<h2>BAXY</h2>" +
            "<a href=\"/\">HOME</a>" +
            "<form enctype=\"multipart/form-data\" action=\"/upload\" method=\"post\">\n" +
            "<input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"2000000\">\n" +
            "File: <input name=\"uploadFile\" type=\"file\"><br>\n" +
            "Path: <input type=\"text\" name=\"path\" value=\"/sdcard/uploads/\"><br>\n" +
            "<input name=\"gezien\" value=\"ja\" type=\"hidden\">\n" +
            "<input type=\"submit\" value=\"Start Upload\" name=\"submitButton\">\n" +
            "</form>" +
            "</body>" +
            "</html>";

    @Override
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response response = null;
        if (method.equals(NanoHTTPD.Method.POST)) {
            String tempFilename = files.get("uploadFile");
            if (tempFilename != null) {
                //rename
                File file = new File(tempFilename);
                try {
                    //
                    String pathString = parms.get("path");
                    if (!pathString.startsWith("/")){
                        pathString = "/sdcard/uploads/";
                    }
                    File path = new File(pathString);
                    path.mkdirs();
                    //
                    file.renameTo(new File(pathString + parms.get("uploadFile")));
                    response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, "OK !");
                } catch (Exception e) {
                    response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Upload Error !");
                }
            } else {
                response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, "Upload Error !");
            }
            return response;
        }
        ///
        StringBuilder sb = new StringBuilder();
        sb.append(page);
        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, sb.toString());
        return response;

    }
}
