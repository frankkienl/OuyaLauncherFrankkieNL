package nl.frankkie.ouyalauncher.web;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import nl.frankkie.ouyalauncher.WebserverActivity;

/**
 * Created by FrankkieNL on 12-8-13.
 */
public class InstallAPK extends WebPage {
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(parms.get("path"))), "application/vnd.android.package-archive");
        WebserverActivity.context.startActivity(intent);
        NanoHTTPD.Response response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, "OK !");
        return response;
    }
}
