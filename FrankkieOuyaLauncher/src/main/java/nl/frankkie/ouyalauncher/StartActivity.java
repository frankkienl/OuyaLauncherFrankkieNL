/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import eu.chainfire.libsuperuser.Shell;
import tv.ouya.console.api.OuyaController;

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
        startImageCaching();
    }

    private void startImageCaching() {
        MakeImageCache task = new MakeImageCache(this);
        task.execute();
    }

    private void initUI(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.main);
        setContentView(R.layout.start);
        Util.setBackground(this);
        Button btnAll = (Button) findViewById(R.id.start_all);
        Button btnGames = (Button) findViewById(R.id.start_games);
        Button btnApps = (Button) findViewById(R.id.start_apps);
        Button btnDiscover = (Button) findViewById(R.id.start_discover);
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_ALL);
                startActivity(i);
            }
        });
        btnGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_OUYA_ONLY);
                startActivity(i);
            }
        });
        btnApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(StartActivity.this, MainActivity.class);
                i.putExtra("type", MainActivity.APP_APP_ONLY);
                startActivity(i);
            }
        });
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDiscover();
            }
        });
        ///FIX for non-OUYA Devices :P
        ((LinearLayout)findViewById(R.id.start_goto_settings)).setOnClickListener(new View.OnClickListener() {
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

        if (event.getKeyCode() == OuyaController.BUTTON_A) {
            goToSettings();
            return true;
        }

        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyDown(keyCode, event);
        return handled || super.onKeyDown(keyCode, event);
    }

    private void goToSettings(){
        Intent i = new Intent();
        i.setClass(this,BackgroundActivity.class);
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


    private class StartDiscoverRootAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Let's do some SU stuff
            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                //String suVersion = Shell.SU.version(false);
                //String suVersionInternal = Shell.SU.version(true);
                List<String> suResult = Shell.SU.run(new String[]{
                        "am start --user 0 -n tv.ouya.console/tv.ouya.console.launcher.store.adapter.DiscoverActivity"
                });

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
                List<String> suResult = Shell.SU.run(new String[]{
                        "am broadcast --user 0 -a tv.ouya.console.action.TURN_OFF"
                });

            }
            return null;
        }

    }
}
