package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by FrankkieNL on 27-7-13.
 */
public class BackgroundMusicActivity extends Activity {

    ArrayList<MyMusic> musics = new ArrayList<MyMusic>();
    ViewGroup table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        initUI();
        initMusic();
    }

    public void initUI(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.music_chooser);
        //Util.setBackground(this);
        Util.setLogo(this);
        table = (ViewGroup) findViewById(R.id.table);
    }

    public void initMusic(){
        File defaultFile = new File("/sdcard/FrankkieOuyaLauncher/music/music_default.mp3");
        if (!defaultFile.exists()){
            File musicFolder = new File("/sdcard/FrankkieOuyaLauncher/music/");
            musicFolder.mkdirs();
            try {
                //add .nomedia
                File noMedia = new File("/sdcard/FrankkieOuyaLauncher/music/.nomedia");
                noMedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            downloadDefaultMusic();
            return;
        }
        MusicsLoaderAsyncTask task = new MusicsLoaderAsyncTask();
        task.execute();
    }

    public void downloadDefaultMusic(){
        DownloadAsyncTask task = new DownloadAsyncTask();
        task.execute("https://raw.github.com/frankkienl/OuyaLauncherFrankkieNL/master/data/music_default.mp3");
    }

    Handler handler = new Handler();
    Context context;
    public void toast(final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void selectMusic(String path) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPreferences.edit().putString(Util.PREFS_MUSIC_FILE, path).commit();
        //start music
        Intent i = new Intent();
        i.setClass(this,BackgroundMusicService.class);
        i.putExtra("cmd",BackgroundMusicService.CMD_RESTART);
        startService(i);
    }

    public class MyMusic{
        String path;
        String name;
        boolean isSelected = false;
    }

    public class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Downloading Default Music");
            dialog.setMessage("Please Wait...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.setProgressNumberFormat("Progress: %1d - %2d");
            dialog.setIndeterminate(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                //Thanks to:
                //http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
                URL url = new URL(urls[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream("/sdcard/FrankkieOuyaLauncher/music/music_default.mp3");

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                toast("Download failed.. (internet is not working)");
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                //ignore
            }
            initMusic();
        }
    }

    public class MusicsLoaderAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            File dir = new File("/sdcard/FrankkieOuyaLauncher/music/");
            String[] list = dir.list();
            musics.clear();
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(BackgroundMusicActivity.this);
            String selectedBackgroundPath = defaultSharedPreferences.getString(Util.PREFS_MUSIC_FILE, "/sdcard/FrankkieOuyaLauncher/music/music_default.png");
            //add silence
            MyMusic musicOff = new MyMusic();
            musicOff.name = "Music Off";
            musicOff.path = "";
            musicOff.isSelected = (selectedBackgroundPath.equals(musicOff.path));
            musics.add(musicOff);
            for (String s : list) {
                if (s.equals(".nomedia")) {
                    continue;
                }
                MyMusic myMusic = new MyMusic();
                File file = new File("/sdcard/FrankkieOuyaLauncher/music/" + s);
                if (!file.exists() || !file.canRead()) {
                    continue;
                }
                try {
                    myMusic.name = s;
                    myMusic.path = file.getPath();
                    myMusic.isSelected = (selectedBackgroundPath.equals(myMusic.path));
                    musics.add(myMusic);
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

    private void fillTable() {
        table.removeAllViews(); //clear
        Runtime.getRuntime().gc();
        ViewGroup row = null;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < musics.size(); i++) {
            if (i % 4 == 0) {
                if (row != null) {
                    table.addView(row);
                }
                row = (ViewGroup) inflater.inflate(R.layout.table_row, table, false);
            }
            View v = fillTable(musics.get(i));
            row.addView(v);
            if (i == musics.size() - 1) {
                table.addView(row);
            }
        }
    }

    private View fillTable(MyMusic myMusic) {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.grid_item, table, false);
        ImageView imageView = (ImageView) layout.findViewById(R.id.item_image);
        TextView textView = (TextView) layout.findViewById(R.id.item_text);
        TextView hiddenTextView = (TextView) layout.findViewById(R.id.item_packagename);
        imageView.setImageResource(R.drawable.music_icon);
        textView.setText(myMusic.name);
        hiddenTextView.setText(myMusic.path);
        final String path = myMusic.path;
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMusic(path);
            }
        });
        return layout;
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
