package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.flurry.android.FlurryAgent;

/**
 * Created by FrankkieNL on 18-7-13.
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.settings);
        Util.setBackground(this);
        Util.setLogo(this);
        Util.setClock(this);

        Button backgroundsBtn = (Button) findViewById(R.id.settings_background);
        backgroundsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(SettingsActivity.this, BackgroundActivity.class);
                startActivity(i);
            }
        });

//        Button logoBtn = (Button) findViewById(R.id.settings_logo);
//        logoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent();
//                i.setClass(SettingsActivity.this, LogoActivity.class);
//                startActivity(i);
//            }
//        });
//        logoBtn.setEnabled(false);

        Button clockAnalog = (Button) findViewById(R.id.settings_analog_clock);
        clockAnalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                prefs.edit().putString("clockType", "analog").commit();
                Util.setClock(SettingsActivity.this);
            }
        });
        Button clockDigital = (Button) findViewById(R.id.settings_digital_clock);
        clockDigital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                prefs.edit().putString("clockType", "digital").commit();
                Util.setClock(SettingsActivity.this);
            }
        });
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
