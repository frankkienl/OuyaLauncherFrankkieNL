package nl.frankkie.ouyalauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.*;
import tv.ouya.console.api.OuyaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    LinearLayout table;
    //    ListView gridView;
    //MyAdapter adapter;
    private static ArrayList<ApplicationInfo> mApplications = new ArrayList<ApplicationInfo>();
    private static ArrayList<ApplicationInfo> mFilteredApplications = new ArrayList<ApplicationInfo>();
    //FILTERS
    public static final int APP_ALL = 0;
    public static final int APP_OUYA_ONLY = 1;
    public static final int APP_APP_ONLY = 2;
    int appType = APP_ALL;
    ////
    Handler handler = new Handler();


    public void showAppInfo() {
        View v = getCurrentFocus();
        if (v != null) {
            TextView tv_packagename = (TextView) ((ViewGroup) v).findViewById(R.id.item_packagename);
            String packagename = tv_packagename.getText().toString();
            showInstalledAppDetails(this, packagename);
        }
    }

    public void showFilters() {
        appType++;
        if (appType > 2) {
            appType = 0;
        }
        filter();
        fillTable();
    }

    public void filter() {
        mFilteredApplications.clear();
        for (ApplicationInfo info : mApplications) {
            if (appType == APP_ALL) {
                mFilteredApplications.add(info);
                continue;
            }
            if (appType == APP_OUYA_ONLY) {
                if (info.isOUYA) {
                    mFilteredApplications.add(info);
                }
            } else if (appType == APP_APP_ONLY) {
                if (!info.isOUYA) {
                    mFilteredApplications.add(info);
                }
            }
        }
    }


    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_22 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OuyaController.init(this);
        initUI();
        appType = getIntent().getIntExtra("type", APP_ALL);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadApplications();
                filter();
                fillTable();
            }
        }, 500);
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
        //Do consume U and Y
        if (event.getKeyCode() == OuyaController.BUTTON_U) {
            showAppInfo();
            return true;
        }
        if (event.getKeyCode() == OuyaController.BUTTON_Y) {
            showFilters();
            return true;
        }
        if (event.getKeyCode() == OuyaController.BUTTON_A) {
            pressedA();
            return true;
        }
        //check menu-key
        if (event.getKeyCode() == OuyaController.BUTTON_MENU) {
            pressedMenu();
            return true;
        }
        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyDown(keyCode, event);
        return handled || super.onKeyDown(keyCode, event);
    }

    public void pressedMenu() {

    }

    public void pressedA() {
//        startDiscover();
    }

    public void startDiscover() {
        Intent i = new Intent();
//        i.setClassName("tv.ouya.console","tv.ouya.console.launcher.store.adapter.DiscoverActivity");
        i.setClassName("tv.ouya.console", "tv.ouya.console.launcher.store.OldDiscoverActivity");
        try {
            startActivity(i);
        } catch (Exception e) {
            Log.e("FrankkieLauncher", "ERROR", e);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Dont consume DPAD, and O
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
        //Do consume U and Y
        if (event.getKeyCode() == OuyaController.BUTTON_U) {
            return true;
        }
        if (event.getKeyCode() == OuyaController.BUTTON_Y) {
            return true;
        }
        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyUp(keyCode, event);
        return handled || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        //Dont consume LS, RS, L2, R2 events
        //boolean handled = OuyaController.onGenericMotionEvent(event);
        //return handled || super.onGenericMotionEvent(event);
        return super.onGenericMotionEvent(event);
    }

    protected void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        Util.setBackground(this);
        table = (LinearLayout) findViewById(R.id.table);
    }


    private void loadApplications() {
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();

            if (mApplications == null) {
                mApplications = new ArrayList<ApplicationInfo>(count);
            }
            mApplications.clear();
            Runtime.getRuntime().gc();

            for (int i = 0; i < count; i++) {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(manager);
                application.setActivity(new ComponentName(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = info.activityInfo.loadIcon(manager);
                application.isOUYA = checkIsOUYAAppByIcon(application);
                mApplications.add(application);
            }
        }
    }

    public boolean checkIsOUYAAppByIcon(ApplicationInfo info) {
        String packageName = "";
        try {
            packageName = info.intent.getComponent().getPackageName();
            android.content.pm.ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
            Resources resources = getPackageManager().getResourcesForApplication(applicationInfo);
            int identifier3 = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");
            if (identifier3 != 0) {
                return true;
            }
//            info.icon = getPackageManager().getResourcesForApplication(applicationInfo).getDrawable(identifier3);
            //Runtime.getRuntime().gc();
            //return true;
        } catch (Exception e) {
            //  Log.e("FRANKKIE_LAUNCHER", "ERROR", e);
        }
        return false;
    }

    public void fillTable() {
        table.removeAllViews(); //clear
        Runtime.getRuntime().gc();
        ViewGroup row = null;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < mFilteredApplications.size(); i++) {
            if (i % 4 == 0) {
                if (row != null) {
                    table.addView(row);
                }
                row = (ViewGroup) inflater.inflate(R.layout.table_row, table, false);
            }
            View v = fillTable(mFilteredApplications.get(i));
            row.addView(v);
            if (i == mFilteredApplications.size() - 1) {
                table.addView(row);
            }
        }
    }

    private Rect mOldBounds = new Rect();

    public View fillTable(final ApplicationInfo info) {
        LinearLayout layout;
        LayoutInflater inflater = getLayoutInflater();
        layout = (LinearLayout) inflater.inflate(R.layout.grid_item, table, false);
        //final ApplicationInfo info = (ApplicationInfo) getItem(id);
        ////////
        ////////

        Drawable icon = info.icon;
        String packageName = info.intent.getComponent().getPackageName();
        info.packagename = packageName;
        if (!info.filtered) {
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
                icon = info.icon = new BitmapDrawable(thumb);
                info.filtered = true;
            }

            /////////////
            //TEST ICON//
            /////////////
            try {
                android.content.pm.ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
                Resources resources = getPackageManager().getResourcesForApplication(applicationInfo);
                int identifier3 = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");
                info.icon = getPackageManager().getResourcesForApplication(applicationInfo).getDrawable(identifier3);
                info.filtered = true;
                Runtime.getRuntime().gc();
            } catch (Exception e) {
                Log.e("FrankkieOuyaLauncher", "ERROR", e);
            }
        }
        ////////
        ////////
        TextView tv = (TextView) layout.findViewById(R.id.item_text);
        tv.setText(info.title);
        TextView tv_packagename = (TextView) layout.findViewById(R.id.item_packagename);
        tv_packagename.setText(packageName);
        ImageView img = (ImageView) layout.findViewById(R.id.item_image);
        img.setImageDrawable(info.icon);
        ///
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = info.intent;
                try {
                    startActivity(i);
                } catch (Exception e) {
                }
            }
        });
        return layout;
    }

}
