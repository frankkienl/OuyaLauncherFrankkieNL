package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by FrankkieNL on 24-7-13.
 */
public class FeedbackActivity extends Activity {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    public void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.feedback);
        //Util.setBackground(this);
        Util.setLogo(this);
        Util.setClock(this);
        editText = (EditText) findViewById(R.id.feedback_ed);
        Button send = (Button) findViewById(R.id.feedback_send_btn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendFeedbackTask task = new SendFeedbackTask(FeedbackActivity.this);
                task.execute(editText.getText().toString());
            }
        });

    }

    public class SendFeedbackTask extends AsyncTask<String, Void, Boolean> {
        Dialog dialog;
        Context context;

        public SendFeedbackTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(context, "Sending feedback", "Please Wait..");
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost("http://frankkie.nl/baxy/feedbackmail.php");
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            String message = strings[0];
            String add = "";
            try {
                int myVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                String myVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                add = "\nVersion: " + myVersion + "-" + myVersionName;
                message += add;
            } catch (PackageManager.NameNotFoundException nnfe) {
                nnfe.printStackTrace();
            }
            params.add(new BasicNameValuePair("message", message));
            try {
                request.setEntity(new UrlEncodedFormEntity(params));
            } catch (UnsupportedEncodingException e) {
                toast("Message cannot be send.. (try sending email instead)");
                return Boolean.FALSE;
            }
            try {
                client.execute(request);
            } catch (IOException e) {
                toast("Feedback cannot be send.. (check internet)");
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean isOk) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                //ignore
            }
            if (isOk) {
                toast("Feedback is send!");
                //Kill activity to prevent duplicates.
                finish();
            }
        }
    }

    Handler handler = new Handler();

    public void toast(final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FeedbackActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });
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
