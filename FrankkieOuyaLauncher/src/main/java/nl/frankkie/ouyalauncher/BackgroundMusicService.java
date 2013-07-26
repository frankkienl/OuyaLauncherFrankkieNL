package nl.frankkie.ouyalauncher;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

/**
 * Created by FrankkieNL on 26-7-13.
 */
public class BackgroundMusicService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MediaPlayer mp = new MediaPlayer();
    }

    public static boolean isAppInForeground(Context context){
        //http://stackoverflow.com/questions/5504632/how-can-i-tell-if-android-app-is-running-in-the-foreground
        if (context.getPackageName().equalsIgnoreCase(((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getPackageName()))
        {
            return true;
        } else {
            return false;
        }
    }
}
