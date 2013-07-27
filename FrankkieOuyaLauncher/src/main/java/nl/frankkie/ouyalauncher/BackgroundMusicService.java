package nl.frankkie.ouyalauncher;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by FrankkieNL on 26-7-13.
 */
public class BackgroundMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    Context context;
    public static final String CMD_START = "start";
    public static final String CMD_RESTART = "restart";
    public static final String CMD_STOP = "stop";
    public static final String CMD_CHECK = "check";

    MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        context = this;
        String command = intent.getStringExtra("cmd");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String filePath = prefs.getString(Util.PREFS_MUSIC_FILE, "");
        if (command.equalsIgnoreCase(CMD_START)) {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            if (filePath.length() > 1) {
                try {
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnErrorListener(this);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (command.equalsIgnoreCase(CMD_STOP)) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } else if (command.equalsIgnoreCase(CMD_RESTART)) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            //
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            if (filePath.length() > 1) {
                try {
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnErrorListener(this);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (command.equalsIgnoreCase(CMD_CHECK)) {
            //The application is calling onStop on an Activity.
            //Check if music should be stopped
            if (!isAppInForeground(this)) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                if (!mediaPlayer.isPlaying()) {
                    if (filePath.length() > 1) {
                        try {
                            mediaPlayer.setDataSource(filePath);
                            mediaPlayer.setOnPreparedListener(this);
                            mediaPlayer.setOnErrorListener(this);
                            mediaPlayer.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static boolean isAppInForeground(Context context) {
        //http://stackoverflow.com/questions/5504632/how-can-i-tell-if-android-app-is-running-in-the-foreground
        if (context.getPackageName().equalsIgnoreCase(((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getPackageName())) {
            Log.e("BAXY", "Baxy is in the foreground :-)");
            return true;
        } else {
            Log.e("BAXY", "Baxy is NOT in the foreground :-( ");
            return false;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (context == null || mediaPlayer == null) {
            return;
        }
        if (isAppInForeground(context)) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        Log.e("BAXY", "MediaPlayer error: " + i + " " + i2);
        return true; //? //http://developer.android.com/reference/android/media/MediaPlayer.OnErrorListener.html
    }
}
