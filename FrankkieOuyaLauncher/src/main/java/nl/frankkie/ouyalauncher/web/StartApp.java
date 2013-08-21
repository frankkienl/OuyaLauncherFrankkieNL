package nl.frankkie.ouyalauncher.web;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import nl.frankkie.ouyalauncher.WebserverActivity;

/**
 * Created by FrankkieNL on 14-8-13.
 */
public class StartApp extends WebPage {

    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response response = null;
        if (WebserverActivity.context == null){
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "App not found, try restarting server");
        }
        PackageManager packageManager = WebserverActivity.context.getPackageManager();
        String packageName = parms.get("packageName");
        if (packageName == null){
            Log.e("BAXY", "/startApp, packagename == null");
        }
//        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        Intent intent = getLaunchIntentForPackage(packageManager,packageName);
        WebserverActivity.context.startActivity(intent);
        //
        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/html", "ok");
        return response;
    }

    public Intent getLaunchIntentForPackage(PackageManager packageManager, String packageName) {
        // First see if the package has an INFO activity; the existence of
        // such an activity is implied to be the desired front-door for the
        // overall package (such as if it has multiple launcher entries).
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN,null);
        intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = packageManager.queryIntentActivities(intentToResolve, 0);

        // Otherwise, try to find a main launcher activity.
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName,
                ris.get(0).activityInfo.name);
        return intent;
    }
}
