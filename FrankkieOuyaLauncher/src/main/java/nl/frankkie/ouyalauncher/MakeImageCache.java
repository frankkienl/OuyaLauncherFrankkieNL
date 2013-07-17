/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MakeImageCache extends AsyncTask<Void, Void, Void> {

    Context context;

    public MakeImageCache(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //Make folder
        File folder = new File("/sdcard/FrankkieOuyaLauncher/thumbnails/");
        folder.mkdirs();
        try {
            //add .nomedia
            File noMedia = new File("/sdcard/FrankkieOuyaLauncher/thumbnails/.nomedia");
            noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        PackageManager packageManager = context.getPackageManager();
        //Query Packagmanager to get list of Games
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.addCategory("tv.ouya.intent.category.GAME");
        List<ResolveInfo> infos = packageManager.queryIntentActivities(mainIntent, 0);
        //Dont forget the OUYA Apps !
        Intent mainIntent2 = new Intent(Intent.ACTION_MAIN, null);
        mainIntent2.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent2.addCategory("tv.ouya.intent.category.APP");
        List<ResolveInfo> infos2 = packageManager.queryIntentActivities(mainIntent2, 0);
        infos.addAll(infos2);
        Resources resources;
        String packageName;
        for (ResolveInfo info : infos) {
            packageName = info.activityInfo.applicationInfo.packageName;
            //Check if image thumbnail for this package already exists.
            File thumbFile = new File("/sdcard/FrankkieOuyaLauncher/thumbnails/" + packageName + ".png");
            if (!thumbFile.exists()) {
                //make
                try {
                    resources = packageManager.getResourcesForApplication(info.activityInfo.applicationInfo);
                    int identifier = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");
                    Drawable ouyaImage = null;
                    if (identifier == 0) {
                        /*
                         * TO THE DEVELOPER OF 'Deep Dungeon of Doom'. RTFM: https://devs.ouya.tv/developers/docs/setup
                         * "The application image that is shown in the launcher is embedded inside of the APK itself.
                         * The expected file is in res/drawable-xhdpi/ouya_icon.png and the image size must be 732x412.";
                        */
                        ouyaImage = info.loadIcon(packageManager);
                    } else {
                        /*
                         * Thank you other developers who read the manual
                         */
                        ouyaImage = packageManager.getResourcesForApplication(info.activityInfo.applicationInfo).getDrawable(identifier);
                    }
                    Bitmap bitmap = ((BitmapDrawable) ouyaImage).getBitmap();
                    // Scale it //http://stackoverflow.com/questions/4609456/android-set-drawable-size-programatically
                    BitmapDrawable d = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, 480, 270, true));
                    //Save to file //http://stackoverflow.com/questions/649154/save-bitmap-to-location
                    FileOutputStream out = new FileOutputStream("/sdcard/FrankkieOuyaLauncher/thumbnails/" + packageName + ".png");
                    d.getBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
                    Runtime.getRuntime().gc(); //important
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
