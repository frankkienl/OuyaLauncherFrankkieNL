package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by FrankkieNL on 18-7-13.
 */
public class LogoActivity extends Activity {

    ViewGroup table;
    ArrayList<MyLogo> logos = new ArrayList<MyLogo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        initLogos();
    }

    private void initLogos() {
        LogosLoaderAsynTask task = new LogosLoaderAsynTask();
        task.execute();
    }

    private void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.logo_settings_screen);
        Util.setBackground(this);
        Util.setLogo(this);
        table = (ViewGroup) findViewById(R.id.table);
    }


    private void fillTable() {
        table.removeAllViews(); //clear
        Runtime.getRuntime().gc();
        ViewGroup row = null;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < logos.size(); i++) {
            if (i % 4 == 0) {
                if (row != null) {
                    table.addView(row);
                }
                row = (ViewGroup) inflater.inflate(R.layout.table_row, table, false);
            }
            View v = fillTable(logos.get(i));
            row.addView(v);
            if (i == logos.size() - 1) {
                table.addView(row);
            }
        }
    }

    private View fillTable(MyLogo logo) {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.grid_item, table, false);
        ImageView imageView = (ImageView) layout.findViewById(R.id.item_image);
        TextView textView = (TextView) layout.findViewById(R.id.item_text);
        TextView hiddenTextView = (TextView) layout.findViewById(R.id.item_packagename);
        imageView.setImageDrawable(logo.drawable);
        textView.setText(logo.name);
        hiddenTextView.setText(logo.path);
        final String path = logo.path;
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectLogo(path);
            }
        });
        return layout;
    }

    private void selectLogo(String path) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPreferences.edit().putString("logoFile", path).commit();
        //Util.logSetBackground(this, path);
        Util.setLogo(this);
    }

    public class MyLogo {
        String path;
        String name;
        Drawable drawable;
        boolean isSelected = false;
    }

    public class LogosLoaderAsynTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            File dir = new File("/sdcard/FrankkieOuyaLauncher/logos/");
            String[] list = dir.list();
            logos.clear();
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(LogoActivity.this);
            String selectedLogoPath = defaultSharedPreferences.getString("logoFile", "/sdcard/FrankkieOuyaLauncher/logos/logo_baxy_white_shadowite_shadow.png");
            for (String s : list) {
                if (s.equals(".nomedia")){
                    continue;
                }
                MyLogo logo = new MyLogo();
                File file = new File("/sdcard/FrankkieOuyaLauncher/logos/" + s);
                if (!file.exists() || !file.canRead()) {
                    continue;
                }
                try {
                    Drawable d = Drawable.createFromPath(file.getPath());
                    logo.drawable = d;
                    logo.name = s;
                    logo.path = file.getPath();
                    logo.isSelected = (selectedLogoPath.equals(logo.path));
                    logos.add(logo);
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
