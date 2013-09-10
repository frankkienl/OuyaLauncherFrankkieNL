package nl.frankkie.ouyalauncher.web;

import android.app.Activity;
import android.app.WallpaperManager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import nl.frankkie.ouyalauncher.WebserverActivity;

/**
 * Created by FrankkieNL on 24-8-13.
 */
public class SetBackground extends WebPage {
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        try {
            String path = parms.get("path");
            FileInputStream fis = new FileInputStream(new File(path));
            ((Activity)WebserverActivity.context).setWallpaper(fis);
            WallpaperManager.getInstance(WebserverActivity.context).suggestDesiredDimensions(1920, 1080);
        } catch (Exception e) {
            e.printStackTrace();
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain","Error\n" + e);
        }
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/plain","OK");
    }
}
