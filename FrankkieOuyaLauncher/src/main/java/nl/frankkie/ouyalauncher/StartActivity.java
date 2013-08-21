/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import nl.frankkie.ouyalauncher.databaserows.DatabaseAppWidget;
import nl.wotuu.database.DatabaseOpenHelper;
import tv.ouya.console.api.OuyaController;

/**
 * Created by FrankkieNL on 6-7-13.
 */
public class StartActivity extends Activity {

    public static final int appWidgetHostId = 1337 + 9001; //Its Over 9000!!Xorz //just some random number
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    AppWidgetHost appWidgetHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OuyaController.init(this);
        initUI();
        context = this;
        startImageCaching();
        initWidgets();
    }

    private void initWidgets() {
        appWidgetHost = new AppWidgetHost(this, appWidgetHostId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = prefs.getBoolean("firstTimeWidgets", true);
        if (firstRun) {
            appWidgetHost.deleteHost();
            prefs.edit().putBoolean("firstTimeWidgets", false).commit();
        }
    }

    public void addWidget() {
        //http://developer.android.com/guide/topics/appwidgets/host.html
        int appWidgetId = this.appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    void addAppWidget(Intent data) {
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        //String customWidget = data.getStringExtra(EXTRA_CUSTOM_WIDGET);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        AppWidgetProviderInfo appWidget = appWidgetManager.getAppWidgetInfo(appWidgetId);

        if (appWidget.configure != null) {
            // Launch over to configure widget, if needed.
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidget.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
            Util.logAddWidgetConfigure(this, appWidget.provider.getPackageName());
        } else {
            // Otherwise, finish adding the widget.
            DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(this);
            DatabaseAppWidget databaseAppWidget = new DatabaseAppWidget();
            databaseAppWidget.info = appWidget;
            databaseAppWidget.appWidgetId = appWidgetId;
            databaseAppWidget.OnInsert();
            placeWidget(appWidgetId, appWidget);
            Util.logAddWidget(this, appWidget.provider.getPackageName());
        }
    }

    public void placeWidget(int appWidgetId, AppWidgetProviderInfo appWidget) {
        View v = appWidgetHost.createView(this, appWidgetId, appWidget);
        //remove clock to make room for widgets
        findViewById(R.id.clock_container).setVisibility(View.GONE);
        ViewGroup group = (ViewGroup) findViewById(R.id.widgets_container);
        //group.removeAllViews();
        group.addView(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PICK_APPWIDGET) {
            addAppWidget(data);
        } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
            // Otherwise, finish adding the widget.
            Log.e("BAXY", "requestCode == REQUEST_CREATE_APPWIDGET");
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            AppWidgetProviderInfo appWidget = appWidgetManager.getAppWidgetInfo(appWidgetId);
            DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(this);
            DatabaseAppWidget databaseAppWidget = new DatabaseAppWidget();
            databaseAppWidget.info = appWidget;
            databaseAppWidget.appWidgetId = appWidgetId;
            databaseAppWidget.OnInsert();
            placeWidget(appWidgetId, appWidget);
        }
    }


    private void startImageCaching() {
        MakeImageCache task = new MakeImageCache(this);
        task.execute();
    }

    private void fixBackgroundOnFirstUse() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            prefs.edit().putBoolean("isFirstRun", false).commit();
            Util.getBackground(this); //place background files on SD
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String path = defaultSharedPreferences.getString("backgroundFile", "/sdcard/FrankkieOuyaLauncher/backgrounds/default.png");
            try {
                FileInputStream fis = new FileInputStream(new File(path));
                setWallpaper(fis);
                WallpaperManager.getInstance(this).suggestDesiredDimensions(1920, 1080);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.main);
        setContentView(R.layout.start);
        //Util.setBackground(this);
        //Fix Background on First Use (update)
        fixBackgroundOnFirstUse();
        ///
        //Util.setLogo(this); moved to refreshWidgets();
        //Util.setClock(this);
        Button btnAll = (Button) findViewById(R.id.start_all);
        Button btnGames = (Button) findViewById(R.id.start_games);
        Button btnApps = (Button) findViewById(R.id.start_apps);
        Button btnAndroid = (Button) findViewById(R.id.start_android);
        Button btnFavorites = (Button) findViewById(R.id.start_favorites);
        Button btnDiscover = (Button) findViewById(R.id.start_discover);
        Button btnSettings = (Button) findViewById(R.id.start_settings);
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_ALL);
                Util.logGoToApplist(StartActivity.this, MainActivity.APP_ALL);
                startActivity(i);
            }
        });
        btnGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_OUYA_GAMES_ONLY);
                Util.logGoToApplist(StartActivity.this, MainActivity.APP_OUYA_GAMES_ONLY);
                startActivity(i);
            }
        });
        btnApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_OUYA_APPS_ONLY);
                Util.logGoToApplist(StartActivity.this, MainActivity.APP_OUYA_APPS_ONLY);
                startActivity(i);
            }
        });
        btnAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_ANDROID_APPS_ONLY);
                Util.logGoToApplist(StartActivity.this, MainActivity.APP_ANDROID_APPS_ONLY);
                startActivity(i);
            }
        });
        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_FAVORITES_ONLY);
                Util.logGoToApplist(StartActivity.this, MainActivity.APP_FAVORITES_ONLY);
                startActivity(i);
            }
        });
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDiscover();
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSettings();
            }
        });
    }

    private void startDiscover() {
        StartDiscoverRootAsyncTask task = new StartDiscoverRootAsyncTask();
        task.execute();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Dont consume DPAD, and O
        int[] ignoreList = new int[]{OuyaController.BUTTON_DPAD_DOWN,
                OuyaController.BUTTON_DPAD_UP,
                OuyaController.BUTTON_DPAD_LEFT,
                OuyaController.BUTTON_DPAD_RIGHT,
                OuyaController.BUTTON_O
        };
        for (int i : ignoreList) {
            if (event.getKeyCode() == i) {
                return super.onKeyDown(keyCode, event); //let the OUYA take care of it.
            }
        }

        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyUp(keyCode, event);
        return handled || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Dont consume DPAD
        int[] ignoreList = new int[]{OuyaController.BUTTON_DPAD_DOWN,
                OuyaController.BUTTON_DPAD_UP,
                OuyaController.BUTTON_DPAD_LEFT,
                OuyaController.BUTTON_DPAD_RIGHT,
                OuyaController.BUTTON_O
        };
        for (int i : ignoreList) {
            if (event.getKeyCode() == i) {
                return super.onKeyDown(keyCode, event); //let the OUYA take care of it.
            }
        }

        if (event.getKeyCode() == OuyaController.BUTTON_Y) {
            turnOuyaOff();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU || event.getKeyCode() == OuyaController.BUTTON_MENU) {
            showMenuDialog();
            return true;
        }

        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyDown(keyCode, event);
        return handled || super.onKeyDown(keyCode, event);
    }

    private void showMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menu");
        String[] items = null;
        if (Util.BETA) {
            items = new String[]{"Launcher Settings", "Running Applications", "Advanced Settings",
                    "Add Widget", "Remove all Widgets",
                    "Turn Off"}; //todo webserver
        } else {
            items = new String[]{"Launcher Settings", "Running Applications", "Advanced Settings",
                    "Turn Off"};
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: {
                        goToSettings();
                        break;
                    }
                    case 1: {
                        goToRunningApps();
                        break;
                    }
                    case 2: {
                        goToAdvancedSettings();
                        break;
                    }
                    case 3: {
                        if (Util.BETA) {
                            addWidget();
                        } else {
                            turnOuyaOff();
                        }
                        break;
                    }
                    case 4: {
                        removeAllWidgets();
                        break;
                    }
                    case 5: {
                        turnOuyaOff();
                        break;
                    }
                    case 6: {
                        startWebserver();
                        break;
                    }
                }
            }
        });
        builder.create().show();
    }

    private void startWebserver() {
        Intent i = new Intent();
        i.setClass(this, WebserverActivity.class);
        startActivity(i);
    }

    private void goToAdvancedSettings() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setAction(Settings.ACTION_SETTINGS);
        startActivity(i);
    }

    private void goToRunningApps() {
        Intent i = new Intent();
        i.setClass(this, RunningAppsActivity.class);
        startActivity(i);
    }

    private void goToSettings() {
        Intent i = new Intent();
        i.setClass(this, SettingsActivity.class);
        Util.logGoToSettings(this);
        startActivity(i);
    }

    private void turnOuyaOff() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Turn off console?");
//        builder.setTitle("OUYA");
        builder.setNeutralButton("Yes (Standby)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TurnOffAsyncTask task = new TurnOffAsyncTask();
                task.execute("standby");
            }
        });
        builder.setPositiveButton("Yes (Really off)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TurnOffAsyncTask task = new TurnOffAsyncTask();
                task.execute("off");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        //Dont consume LS, RS, L2, R2 events
        //boolean handled = OuyaController.onGenericMotionEvent(event);
        //return handled || super.onGenericMotionEvent(event);
        return super.onGenericMotionEvent(event);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //ANALYTICS
        FlurryAgent.onStartSession(this, "MDHSMF65TV4JCSW3QN63");
        //
        Util.onStart(this);
        //Update Check
        Updater updater = Updater.getInstance(this);
        updater.startUpdateCheck();
        //Widgets
        appWidgetHost.startListening();
        refreshWidgets();
    }

    public void removeAllWidgets() {
        ViewGroup group = (ViewGroup) findViewById(R.id.widgets_container);
        group.removeAllViews();
        //Get Clock Back :P
        findViewById(R.id.clock_container).setVisibility(View.VISIBLE);
        //
        DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(this);
        Cursor cursor = helper.WriteableDatabase.rawQuery("SELECT id FROM appwidget", null);
        ArrayList<Integer> ids = new ArrayList<Integer>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            ids.add(id);
        }
        cursor.close();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        for (int id : ids) {
            DatabaseAppWidget databaseAppWidget = new DatabaseAppWidget(id);
            //databaseAppWidget.OnLoad();
            appWidgetHost.deleteAppWidgetId(id);
            databaseAppWidget.OnDelete();
        }
    }

    public void refreshWidgets() {
        Util.setClock(this);
        Util.setLogo(this);
        //check existing widgets!
        DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(this);
        Cursor cursor = helper.WriteableDatabase.rawQuery("SELECT id FROM appwidget", null);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            DatabaseAppWidget databaseAppWidget = new DatabaseAppWidget(id);
            databaseAppWidget.OnLoad();
            AppWidgetProviderInfo appWidget = appWidgetManager.getAppWidgetInfo(databaseAppWidget.appWidgetId);
            databaseAppWidget.info = appWidget;
            placeWidget(databaseAppWidget.appWidgetId, appWidget);
        }
        cursor.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Util.onStop(this);
        //Widgets
        appWidgetHost.stopListening();
        //ANALYTICS
        FlurryAgent.onEndSession(this);
    }

    private class StartDiscoverRootAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Let's do some SU stuff
            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                //String suVersion = Shell.SU.version(false);
                //String suVersionInternal = Shell.SU.version(true);
                Util.logStartDiscover(StartActivity.this);
                List<String> suResult = Shell.SU.run(new String[]{
                        "am start --user 0 -n tv.ouya.console/tv.ouya.console.launcher.store.adapter.DiscoverActivity"
                });
            } else {
                toast(context, "Root is not Available.. Starting Stock Launcher");
                Intent i = new Intent();
                i.setClassName("tv.ouya.console", "tv.ouya.console.launcher.OverlayMenuActivity");
                try {
                    context.startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

    }

    private class TurnOffAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            boolean reallyOff = params[0].equals("off"); //else standby
            // Let's do some SU stuff
            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                //String suVersion = Shell.SU.version(false);
                //String suVersionInternal = Shell.SU.version(true);
                Util.logTurnOff(StartActivity.this);
                if (!reallyOff) {
                    List<String> suResult = Shell.SU.run(new String[]{
                            "am broadcast --user 0 -a tv.ouya.console.action.TURN_OFF"
                    });
                } else {
                    List<String> suResult = Shell.SU.run(new String[]{
                            "reboot -p"
                            /*
                            "input keyevent 26",
                            http://forum.xda-developers.com/showthread.php?t=2063741
                            //GETEVENT
                            "sendevent /dev/input/event1 0001 0074 00000001",
                            "sendevent /dev/input/event1 0000 0000 00000000",
                            "sleep 2",
                            "sendevent /dev/input/event1 0001 0074 00000000",
                            "sendevent /dev/input/event1 0000 0000 00000000",
                            ///XDA
                            "sendevent /dev/input/event0 0001 116 1",
                            "sendevent /dev/input/event0 0000 0000 00000000",
                            "sleep 2",
                            "sendevent /dev/input/event0 0001 116 00000000",
                            "sendevent /dev/input/event0 0000 0000 00000000"
                            */
                    });
                    StringBuilder sb = new StringBuilder();
                    if (suResult != null) {
                        for (String line : suResult) {
                            sb.append(line).append((char)10);
                        }
                    }
                    Log.e("BAXY","BAXY\n" + sb.toString());
                }
            } else {
                toast(context, "Root is not Available..");
            }
            return null;
        }
    }

    Handler handler = new Handler();
    Context context;

    public void toast(final Context context, final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        });
    }
}
