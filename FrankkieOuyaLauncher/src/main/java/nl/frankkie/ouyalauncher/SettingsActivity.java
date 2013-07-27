package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

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
        //Util.setBackground(this);
        Util.setLogo(this);
        Util.setClock(this);

//        Button backgroundsBtn = (Button) findViewById(R.id.settings_background);
//        backgroundsBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent();
//                i.setClass(SettingsActivity.this, BackgroundActivity.class);
//                startActivity(i);
//            }
//        });

        Button liveWallpaperBtn = (Button) findViewById(R.id.settings_live_wallpaper);
        liveWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                //i.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                i.setAction(Intent.ACTION_SET_WALLPAPER);
                //i.setComponent(new ComponentName("com.android.wallpaper.livepicker",".LiveWallpaperActivity"));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button logoBaxyBtn = (Button) findViewById(R.id.settings_logo_baxy);
        logoBaxyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                prefs.edit().putString("logoType", "BAXY").commit();
                Util.setLogo(SettingsActivity.this);
            }
        });

        Button logoOuyaBtn = (Button) findViewById(R.id.settings_logo_ouya);
        logoOuyaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                prefs.edit().putString("logoType", "OUYA").commit();
                Util.setLogo(SettingsActivity.this);
            }
        });

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
        final ToggleButton betaToggleButton = (ToggleButton) findViewById(R.id.settings_version_beta_togglebutton);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        betaToggleButton.setChecked(prefs.getBoolean(Util.PREFS_BETA_ENABLED, false));
        betaToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setTitle("Are you sure?");
                    builder.setMessage("Are you sure you want enable BETA?\nThe BETA-version Will have bugs and Strange Features!\nIf you are not sure you can handle that stick with stable releases.");
                    builder.setPositiveButton("Enable BETA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            prefs.edit().putBoolean(Util.PREFS_BETA_ENABLED, true).commit();
                        }
                    });
                    builder.setNegativeButton("No thank you", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            prefs.edit().putBoolean(Util.PREFS_BETA_ENABLED, false).commit();
                            betaToggleButton.setChecked(false);
                        }
                    });
                    builder.create().show();
                } else {
                    prefs.edit().putBoolean(Util.PREFS_BETA_ENABLED, false).commit();
                }
            }
        });
        Button feedbackBtn = (Button) findViewById(R.id.settings_feedback_btn);
        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(SettingsActivity.this, FeedbackActivity.class);
                startActivity(i);
            }
        });

        Button musicBtn = (Button) findViewById(R.id.settings_music);
        musicBtn.setVisibility(View.GONE); //BETA
        musicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(SettingsActivity.this, BackgroundMusicActivity.class);
                startActivity(i);
            }
        });

        TextView versionTextView = (TextView) findViewById(R.id.settings_version_tv);
        try {
            int myVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            String myVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionTextView.setText("Version: " + myVersion + "-" + myVersionName);
        } catch (PackageManager.NameNotFoundException nnfe) {
            nnfe.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Util.onStart(this);
        //ANALYTICS
        FlurryAgent.onStartSession(this, "MDHSMF65TV4JCSW3QN63");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Util.onStop(this);
        //ANALYTICS
        FlurryAgent.onEndSession(this);
    }
}
