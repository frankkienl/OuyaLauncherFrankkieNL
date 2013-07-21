/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import eu.chainfire.libsuperuser.Shell;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ouya.console.api.OuyaController;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by FrankkieNL on 6-7-13.
 */
public class StartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OuyaController.init(this);
        initUI();
        context = this;
        startImageCaching();
    }

    private void startImageCaching() {
        MakeImageCache task = new MakeImageCache(this);
        task.execute();
    }

    private void fixBackgroundOnFirstUse(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        if (isFirstRun){
            prefs.edit().putBoolean("isFirstRun", false).commit();
            Util.getBackground(this); //place background files on SD
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String path = defaultSharedPreferences.getString("backgroundFile", "/sdcard/FrankkieOuyaLauncher/backgrounds/default.png");
            try {
                FileInputStream fis = new FileInputStream(new File(path));
                setWallpaper(fis);
                WallpaperManager.getInstance(this).suggestDesiredDimensions(1920,1080);
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
        Util.setLogo(this);
        Util.setClock(this);
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
        String[] items = new String[]{"Launcher Settings", "Running Applications", "Advanced Settings", "Turn Off"};
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
                        turnOuyaOff();
                        break;
                    }
                }
            }
        });
        builder.create().show();
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
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TurnOffAsyncTask task = new TurnOffAsyncTask();
                task.execute();
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
        //Update Check
        Updater updater = Updater.getInstance(this);
        updater.startUpdateCheck();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    private class TurnOffAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Let's do some SU stuff
            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                //String suVersion = Shell.SU.version(false);
                //String suVersionInternal = Shell.SU.version(true);
                Util.logTurnOff(StartActivity.this);
                List<String> suResult = Shell.SU.run(new String[]{
                        "am broadcast --user 0 -a tv.ouya.console.action.TURN_OFF"
                });
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
