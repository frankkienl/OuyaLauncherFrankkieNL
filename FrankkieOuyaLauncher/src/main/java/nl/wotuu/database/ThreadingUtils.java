package nl.wotuu.database;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

/**
 * Created by Wouter on 6/16/13.
 */
public class ThreadingUtils {

    /**
     * Shows a toast message on the UI thread.
     *
     * @param activity The activity to use.
     * @param message  The message to show.
     * @param length   The length of the toast message.
     */
    public static void ShowToastOnUiThread(final Activity activity, final String message, final int length) {

        final Runnable mUpdateResults = new Runnable() {
            public void run() {
                Toast.makeText(activity, message, length).show();
            }
        };

        activity.runOnUiThread(mUpdateResults);
    }

    /**
     * Starts an async task, running it on the executor thread pool if need be (fixes a bug).
     * @param task The task to start.
     * @param params The parameters to pass to the async task when starting.
     */
    public static <T> void StartASyncTask(AsyncTask task, T... params){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            task.execute(params);
    }
}
