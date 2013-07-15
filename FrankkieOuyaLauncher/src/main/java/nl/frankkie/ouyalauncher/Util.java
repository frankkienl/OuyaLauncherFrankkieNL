/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by FrankkieNL on 10-7-13.
 * http://stackoverflow.com/questions/5834221/android-drawable-from-file-path
 */
public class Util {

    public static String loadedBackgroundString;
    public static Drawable loadedBackground;

    public static void setBackground(Activity activity) {
        try {
            activity.findViewById(R.id.layout_background).setBackground(getBackground(activity));
        } catch (Exception e) {
            //If this happens, you have bigger problems than a missing background..
            //You have a missing layout-root.
            //The app should crash right about now :P
            e.printStackTrace();
        }
    }

    public static Drawable getBackground(Context c) {
        //Check if default Background exists
        //Background file should always exist!
        File defaultFile = new File("/sdcard/FrankkieOuyaLauncher/backgrounds/default.png");
        if (!defaultFile.exists()) {
            //make that file
            File folder = new File("/sdcard/FrankkieOuyaLauncher/backgrounds/");
            folder.mkdirs();
            try {
                copyResourceToFile(c, R.raw.bg_color, new File("/sdcard/FrankkieOuyaLauncher/backgrounds/default.png"));
                copyResourceToFile(c, R.raw.ouya_background, new File("/sdcard/FrankkieOuyaLauncher/backgrounds/ouya_controller.png"));
                copyResourceToFile(c, R.raw.ouya_console_wallpaper, new File("/sdcard/FrankkieOuyaLauncher/backgrounds/ouya_console.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Return Default !
            return c.getResources().getDrawable(R.drawable.bg_color);
        }

        //Check preference
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        String fileString = defaultSharedPreferences.getString("backgroundFile", "/sdcard/FrankkieOuyaLauncher/backgrounds/default.png");
        File file = new File(fileString);
        if (!file.exists()) {
            //The selected custom background does not exist..
            //Return Default !
            Log.e("FrankkieOuyaLauncher", "Selected Background does not exist !! (return default)");
            return c.getResources().getDrawable(R.drawable.bg_color);
        }
        //File does exist
        //Check if already loaded
        if (loadedBackgroundString != null && loadedBackground != null) {
            if (loadedBackgroundString.equals(fileString)) {
                return loadedBackground;
            }
        }
        //Not loaded yet, load it, return it
        try {
            Drawable d = Drawable.createFromPath(fileString);
            loadedBackground = d;
            loadedBackgroundString = fileString;
            return d;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Default
        return c.getResources().getDrawable(R.drawable.bg_color);
    }

    public static void copyResourceToFile(Context c, int resourceId, File file) throws IOException {
        //http://stackoverflow.com/questions/8664468/copying-raw-file-into-sdcard
        InputStream in = c.getResources().openRawResource(resourceId);
        FileOutputStream out = new FileOutputStream(file);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    // Favorites

    public static void addToFavorites(Context context, String packagename) {
        List<String> list = getFavorites(context);
        list.add(packagename);
        JSONArray arr = new JSONArray();
        for (String s : list){
            arr.put(s);
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("favorites", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = obj.toString();
        setFavoritesJSON(context,json);
    }

    public static void removeFromFavorites(Context context, String packagename) {
        List<String> list = getFavorites(context);
        list.remove(packagename);
        JSONArray arr = new JSONArray();
        for (String s : list){
            arr.put(s);
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("favorites", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = obj.toString();
        setFavoritesJSON(context,json);
    }

    public static List<String> getFavorites(Context context) {
        String json = getFavoritesJSON(context);
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray arr = obj.getJSONArray("favorites");
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < arr.length(); i++){
                list.add(arr.getString(i));
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getFavoritesJSON(Context context) {
        //Just use prefs, no need for a DB for such a small list
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("favorites", "{\"favorites\":[]}"); //empty array
    }

    private static void setFavoritesJSON(Context context, String json) {
        //Just use prefs, no need for a DB for such a small list
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("favorites", json).commit();
    }


    //Analytics

    public static void logAppLaunch(Context context, AppInfo info) {
        //Log this applaunch to Analytics
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("packagename", info.packagename);
        params.put("appname", info.title.toString());
        params.put("isOUYA", "" + info.isOUYA);
        params.put("isOUYAGame", "" + info.isOUYAGame);
        FlurryAgent.logEvent("AppLaunch", params);
    }

    public static void logAppInfo(Context context, String packagename) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("packagename", packagename);
        FlurryAgent.logEvent("AppInfo", params);
    }

    public static void logFilterChange(Context context, int newFilter) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("filter", "" + newFilter);
        FlurryAgent.logEvent("AppFilterChange", params);
    }

    public static void logStartDiscover(Context context) {
        FlurryAgent.logEvent("startDiscover");
    }

    public static void logTurnOff(Context context) {
        FlurryAgent.logEvent("turnOff");
    }

    public static void logGoToSettings(Context context) {
        FlurryAgent.logEvent("goToSettings");
    }

    public static void logGoToApplist(Context context, int newFilter) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("filter", "" + newFilter);
        FlurryAgent.logEvent("Applist", params);
    }

    public static void logSetBackground(Context context, String path) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("path", path);
        FlurryAgent.logEvent("setBackground", params);
    }

    public static void logAddFavorite(Context context, String packagename){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("packagename", packagename);
        FlurryAgent.logEvent("AddFavorite", params);
    }

    public static void logRemoveFavorite(Context context, String packagename){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("packagename", packagename);
        FlurryAgent.logEvent("AddRemove", params);
    }

}
