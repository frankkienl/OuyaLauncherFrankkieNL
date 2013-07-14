/*
 * Copyright (c) 2013. FrankkieNL
 */

package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;
import tv.ouya.console.api.OuyaController;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by FrankkieNL on 10-7-13.
 */
public class BackgroundActivity extends Activity {

    LinearLayout table;
    ArrayList<MyBackground> backgrounds = new ArrayList<MyBackground>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OuyaController.init(this);
        initUI();
        initBackgrounds();
    }

    private void initBackgrounds() {
        BackgroundsLoaderAsynTask task = new BackgroundsLoaderAsynTask();
        task.execute();
    }

    private void fillTable() {
        table.removeAllViews(); //clear
        Runtime.getRuntime().gc();
        ViewGroup row = null;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < backgrounds.size(); i++) {
            if (i % 4 == 0) {
                if (row != null) {
                    table.addView(row);
                }
                row = (ViewGroup) inflater.inflate(R.layout.table_row, table, false);
            }
            View v = fillTable(backgrounds.get(i));
            row.addView(v);
            if (i == backgrounds.size() - 1) {
                table.addView(row);
            }
        }
    }

    private View fillTable(MyBackground background) {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.grid_item, table, false);
        ImageView imageView = (ImageView) layout.findViewById(R.id.item_image);
        TextView textView = (TextView) layout.findViewById(R.id.item_text);
        TextView hiddenTextView = (TextView) layout.findViewById(R.id.item_packagename);
        imageView.setImageDrawable(background.drawable);
        textView.setText(background.name);
        hiddenTextView.setText(background.path);
        final String path = background.path;
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBackground(path);
            }
        });
        return layout;
    }

    private void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.background_chooser);
        Util.setBackground(this);
        table = (LinearLayout) findViewById(R.id.table);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Dont consume DPAD, and O
        int[] ignoreList = new int[]{OuyaController.BUTTON_DPAD_DOWN,
                OuyaController.BUTTON_DPAD_UP,
                OuyaController.BUTTON_DPAD_LEFT,
                OuyaController.BUTTON_DPAD_RIGHT,
                OuyaController.BUTTON_A,
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
                OuyaController.BUTTON_A,
                OuyaController.BUTTON_O
        };
        for (int i : ignoreList) {
            if (event.getKeyCode() == i) {
                return super.onKeyDown(keyCode, event); //let the OUYA take care of it.
            }
        }

        //Let the SDK take care of the rest
        boolean handled = OuyaController.onKeyDown(keyCode, event);
        return handled || super.onKeyDown(keyCode, event);
    }

    private void selectBackground(String path) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPreferences.edit().putString("backgroundFile", path).commit();
        Util.logSetBackground(this, path);
        Util.setBackground(this);
    }

    public class MyBackground {
        String path;
        String name;
        Drawable drawable;
        boolean isSelected = false;
    }

    public class BackgroundsLoaderAsynTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            File dir = new File("/sdcard/FrankkieOuyaLauncher/backgrounds/");
            String[] list = dir.list();
            backgrounds.clear();
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(BackgroundActivity.this);
            String selectedBackgroundPath = defaultSharedPreferences.getString("backgroundFile", "/sdcard/FrankkieOuyaLauncher/backgrounds/default.png");
            for (String s : list) {
                MyBackground background = new MyBackground();
                File file = new File("/sdcard/FrankkieOuyaLauncher/backgrounds/" + s);
                if (!file.exists() || !file.canRead()) {
                    continue;
                }
                try {
                    Drawable d = Drawable.createFromPath(file.getPath());
                    background.drawable = d;
                    background.name = s;
                    background.path = file.getPath();
                    background.isSelected = (selectedBackgroundPath.equals(background.path));
                    backgrounds.add(background);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            fillTable();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //ANALYTICS
        FlurryAgent.onStartSession(this, "MDHSMF65TV4JCSW3QN63");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //ANALYTICS
        FlurryAgent.onEndSession(this);
    }
}
