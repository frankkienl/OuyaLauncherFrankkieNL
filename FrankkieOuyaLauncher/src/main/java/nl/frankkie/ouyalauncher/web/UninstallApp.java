package nl.frankkie.ouyalauncher.web;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import nl.frankkie.ouyalauncher.WebserverActivity;

/**
 * Created by FrankkieNL on 14-8-13.
 */
public class UninstallApp extends WebPage {

    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response response = null;
        if (WebserverActivity.context == null){
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "App not found, try restarting server");
        }

        Uri packageUri = Uri.parse("package:" + parms.get("packageName"));
        Intent intent =
                //new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri); //4.0+ only
                new Intent(Intent.ACTION_DELETE, packageUri); //ALL
        WebserverActivity.context.startActivity(intent);
        //
        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/html", "ok");
        return response;
    }
}
