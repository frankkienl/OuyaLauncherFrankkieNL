package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.*;

/**
 * Created by Gebruiker on 10-7-13.
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
                copyResourceToFile(c, R.raw.bg, new File("/sdcard/FrankkieOuyaLauncher/backgrounds/default.png"));
                copyResourceToFile(c, R.raw.ouya_background, new File("/sdcard/FrankkieOuyaLauncher/backgrounds/ouya_controller.png"));
                copyResourceToFile(c, R.raw.ouya_console_wallpaper, new File("/sdcard/FrankkieOuyaLauncher/backgrounds/ouya_console.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Return Default !
            return c.getResources().getDrawable(R.drawable.bg);
        }

        //Check preference
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        String fileString = defaultSharedPreferences.getString("backgroundFile", "/sdcard/FrankkieOuyaLauncher/backgrounds/default.png");
        File file = new File(fileString);
        if (!file.exists()) {
            //The selected custom background does not exist..
            //Return Default !
            Log.e("FrankkieOuyaLauncher", "Selected Background does not exist !! (return default)");
            return c.getResources().getDrawable(R.drawable.bg);
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
        return c.getResources().getDrawable(R.drawable.bg);
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
}
