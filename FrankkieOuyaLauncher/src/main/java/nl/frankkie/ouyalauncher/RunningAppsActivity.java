package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import eu.chainfire.libsuperuser.Shell;
import tv.ouya.console.api.OuyaController;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by FrankkieNL on 14-7-13.
 */
public class RunningAppsActivity extends Activity {

    LinearLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OuyaController.init(this);
        initUI();
        getRunningTasks();
    }

    private void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.running_apps);
        Util.setBackground(this);
        table = (LinearLayout) findViewById(R.id.table);
    }

    public static final int numberOfItemsPerRow = 5;

    private void getRunningTasks() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(100);

        table.removeAllViews(); //clear
        ViewGroup row = null;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < runningTasks.size(); i++) {
            if (i % numberOfItemsPerRow == 0) {
                if (row != null) {
                    table.addView(row);
                }
                row = (ViewGroup) inflater.inflate(R.layout.table_row, table, false);
            }
            View v = getView(runningTasks.get(i));
            row.addView(v);
            if (i == runningTasks.size() - 1) {
                table.addView(row);
            }
        }
    }

    public View getView(final ActivityManager.RunningTaskInfo info) {
        LinearLayout layout;
        LayoutInflater inflater = getLayoutInflater();
        layout = (LinearLayout) inflater.inflate(R.layout.grid_item, table, false);
        //final AppInfo info = (AppInfo) getItem(id);
        ////////
        String packageName = info.baseActivity.getPackageName();
        ////////
        ////////
        TextView tv = (TextView) layout.findViewById(R.id.item_text);
        tv.setText(getAppNameByPackagename(packageName));
        TextView tv_packagename = (TextView) layout.findViewById(R.id.item_packagename);
        tv_packagename.setText(packageName);
        //
        ImageView img = (ImageView) layout.findViewById(R.id.item_image);
        File thumbFile = new File("/sdcard/FrankkieOuyaLauncher/thumbnails/" + packageName + ".png");
        if (thumbFile.exists()) {
            img.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeFile(thumbFile.getPath())));
        } else {
            Drawable drawable = getIconImageOUYA(info);
            if (drawable == null) {
                drawable = getIconImageAndroidApps(info);
            }
            if (drawable == null) { //fallback
                drawable = getResources().getDrawable(R.drawable.ic_launcher);
            }
            img.setImageDrawable(drawable);
        }
        ///
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setComponent(info.baseActivity);
                try {
                    //Analytics
//                    Util.logAppLaunch(RunningAppsActivity.this, info);
                    startActivity(i);
                } catch (Exception e) {
                }
            }
        });
        return layout;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Dont consume DPAD
        int[] ignoreList = new int[]{OuyaController.BUTTON_DPAD_DOWN,
                OuyaController.BUTTON_DPAD_UP,
                OuyaController.BUTTON_DPAD_LEFT,
                OuyaController.BUTTON_DPAD_RIGHT,
                OuyaController.BUTTON_O,
                OuyaController.BUTTON_A
        };
        for (int i : ignoreList) {
            if (event.getKeyCode() == i) {
                return super.onKeyDown(keyCode, event); //let the OUYA take care of it.
            }
        }
        if (event.getKeyCode() == OuyaController.BUTTON_Y) {
            killApp();
            return true;
        }
        if (event.getKeyCode() == OuyaController.BUTTON_U) {
            //REFRESH :P
            getRunningTasks();
            return true;
        }
        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyDown(keyCode, event);
        return handled || super.onKeyDown(keyCode, event);
    }


    public String getAppNameByPackagename(String packagename) {
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packagename, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : packagename);
        return applicationName;
    }

    public Drawable getIconImageOUYA(ActivityManager.RunningTaskInfo info) {
        String packageName = info.baseActivity.getPackageName();
        try {
            android.content.pm.ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
            Resources resources = getPackageManager().getResourcesForApplication(applicationInfo);
            int identifier3 = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");
            Drawable drawable = getPackageManager().getResourcesForApplication(applicationInfo).getDrawable(identifier3);
            ///////////////////
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            // Scale it //http://stackoverflow.com/questions/4609456/android-set-drawable-size-programatically
            BitmapDrawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 480, 270, true));
            //Save to file //http://stackoverflow.com/questions/649154/save-bitmap-to-location
            FileOutputStream out = new FileOutputStream("/sdcard/FrankkieOuyaLauncher/thumbnails/" + packageName + ".png");
            d.getBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
            Runtime.getRuntime().gc(); //important
            return drawable;
        } catch (Exception e) {
            Log.e("FrankkieOuyaLauncher", "ERROR", e);
            return null;
        }
    }

    private Rect mOldBounds = new Rect();

    public Drawable getIconImageAndroidApps(ActivityManager.RunningTaskInfo info) {
        Drawable icon = null;
        PackageManager packageManager = getPackageManager();
        try {
            icon = packageManager.getActivityIcon(info.baseActivity);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
//        Drawable icon = info.icon;
        //final Resources resources = getContext().getResources();
        int width = 180;//(int) resources.getDimension(android.R.dimen.app_icon_size);
        int height = 180;//(int) resources.getDimension(android.R.dimen.app_icon_size);

        final int iconWidth = icon.getIntrinsicWidth();
        final int iconHeight = icon.getIntrinsicHeight();

        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        }

        if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
            final float ratio = (float) iconWidth / iconHeight;

            if (iconWidth > iconHeight) {
                height = (int) (width / ratio);
            } else if (iconHeight > iconWidth) {
                width = (int) (height * ratio);
            }

            final Bitmap.Config c =
                    icon.getOpacity() != PixelFormat.OPAQUE ?
                            Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            final Bitmap thumb = Bitmap.createBitmap(width, height, c);
            final Canvas canvas = new Canvas(thumb);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
            // Copy the old bounds to restore them later
            // If we were to do oldBounds = icon.getBounds(),
            // the call to setBounds() that follows would
            // change the same instance and we would lose the
            // old bounds
            mOldBounds.set(icon.getBounds());
            icon.setBounds(0, 0, width, height);
            icon.draw(canvas);
            icon.setBounds(mOldBounds);
            icon = new BitmapDrawable(thumb);
            return icon;
        }
        return null;
    }

    public void killApp() {
        View v = getCurrentFocus();
        if (v != null) {
            TextView tv_packagename = (TextView) ((ViewGroup) v).findViewById(R.id.item_packagename);
            String packagename = tv_packagename.getText().toString();
            killApp(packagename);
//            Util.logAppInfo(MainActivity.this, packagename);
        }
    }

    public void killApp(String packagename) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        try {
            //Method 1: ActivityManager
            activityManager.killBackgroundProcesses(packagename); //THIS DOES NOT WORK ?! :C !
//            android.os.Process.killProcess();
            //Method 2: ActivityManager via Root # am kill --user 0 <packagename>
            KillAppAsyncTask task = new KillAppAsyncTask(); //THIS DOES NOT WORK ?! :C !
            task.execute(packagename);
            //Method 3: Process
            int pid = getPid(packagename); //THIS DOES NOT WORK ?! :C !
            android.os.Process.killProcess(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPid(String packagename){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses){
            //Look for the process of this packagename
            for (String pak : info.pkgList){
                if (pak.equals(packagename)){
                    return info.pid;
                }
            }
        }
        return -1;
    }

    private class KillAppAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            // Let's do some SU stuff
            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                //String suVersion = Shell.SU.version(false);
                //String suVersionInternal = Shell.SU.version(true);
                //WHY CANT I KILL APPS ?!
                //I tried adb shell am kill-all, but nothing happens...
                //Its almost as if they are not backgroundprocesses..
                List<String> suResult = Shell.SU.run(new String[]{
                        "am kill --user 0 " + params[0]
                });
                if (suResult.size() != 0) {
                    System.out.println(suResult.get(0));
                }
            }
            return null;
        }
    }
}
