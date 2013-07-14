/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by FrankkieNL on 14-7-13.
 */
public class Updater {

    private static Updater instance;
    Context context;
    Handler handler = new Handler();
    public static final String apkName = "FrankkieOuyaLauncher.apk";
    public static final String versionJsonUrl = "https://raw.github.com/frankkienl/OuyaLauncherFrankkieNL/master/version.json";
    public static final String apkUrl = "https://raw.github.com/frankkienl/OuyaLauncherFrankkieNL/master/FrankkieOuyaLauncher/FrankkieOuyaLauncher.apk";

    public void startUpdateCheck(){
        UpdateCheckAsyncTask task = new UpdateCheckAsyncTask();
        task.execute();
    }

    private Updater(Context context){
        this.context = context;
    }

    public static Updater getInstance(Context context){
        if (instance == null){
            instance = new Updater(context);
        }
        return instance;
    }

    public void toast(final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    public class UpdateCheckAsyncTask extends AsyncTask<Object, Object, JSONObject> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            //Dont show dialog for this
            //dialog = ProgressDialog.show(context, "Version Check", "Please Wait...");
        }

        @Override
        protected JSONObject doInBackground(Object... objects) {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("https://raw.github.com/frankkienl/OuyaLauncherFrankkieNL/master/version.json");
            try {
                HttpResponse response = client.execute(request);
                JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
                int version = jsonObject.getInt("newestVersion");
                String changes = jsonObject.getString("changes");
                try {
                    //Get versionCode from Manifest
                    int myVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                    if (myVersion < version) {
                        return jsonObject; //boolean as Object
                    } else {
                        toast("This app is up to date :-) !");
                        return null;
                    }
                } catch (PackageManager.NameNotFoundException nnfe) {
                    toast("Version check failed.. (packagename not found)");
                }
            } catch (IOException e) {
                toast("Version check failed.. (internet is not working)");
            } catch (JSONException je) {
                toast("Version check failed.. (json-parser is not working)");
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
               // dialog.dismiss();
            } catch (Exception e) {
                //ignore
            }

            if (jsonObject != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("New Version Available !");
                String changes = "";
                try {
                    String temp = jsonObject.getString("changes");
                    changes = "Changes:\n" + temp + "\n";
                } catch (JSONException e){
                    //ignore
                }
                builder.setMessage(changes + "Download now?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        downloadViaAsyncTask();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //nothing, just remove dialog
                    }
                });
                builder.create().show();
            }
        }
    }


    public void downloadViaAsyncTask() {
        DownloadAsyncTask task = new DownloadAsyncTask();
        task.execute("Sure"); //some string, doesn't care
    }

    public class DownloadAsyncTask extends AsyncTask<Object, Integer, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Downloading Update");
            dialog.setMessage("Please Wait...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.setProgressNumberFormat("Progress: %1d - %2d");
            dialog.setIndeterminate(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Object... objects) {
            try {
                //Thanks to:
                //http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
                URL url = new URL("https://raw.github.com/frankkienl/OuyaLauncherFrankkieNL/master/FrankkieOuyaLauncher/FrankkieOuyaLauncher.apk");
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/FrankkieOuyaLauncher.apk");

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                return true;
            } catch (Exception e) {
                toast("Download failed.. (internet is not working)");
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                //ignore
            }
            if (success) {
                try {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_INSTALL_PACKAGE);
                    i.setData(Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/FrankkieOuyaLauncher.apk"));
                    context.startActivity(i);
                } catch (Exception e) {
                    toast("Cannot update application.. Please use a filemanager (like Total Commander) and select 'FrankkieOuyaLauncher.apk'.");
                }
            }
        }
    }
}
