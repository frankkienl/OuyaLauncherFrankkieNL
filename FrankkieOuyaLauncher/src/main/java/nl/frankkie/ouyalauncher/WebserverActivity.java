package nl.frankkie.ouyalauncher;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by FrankkieNL on 4-8-13.
 */
public class WebserverActivity extends Activity {

    TextView tv1;
    TextView tv2;
    StringBuilder tv2sb;
    int portNumber = 1234;
    public static Context context;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        initUI();
        initWebserverStuff();
        initQR();
    }

    public void initQR() {
        ImageView imageView = (ImageView) findViewById(R.id.webserver_qr);
        //String adres = "http%3A%2F%2F192.168.1.108%3A1234%2F";
        String adres = "http%3A%2F%2F" + getIPAddress(true) + "%3A" + portNumber + "%2F";
        UrlImageViewHelper.setUrlDrawable(imageView, "http://chart.apis.google.com/chart?cht=qr&chs=512x512&chld=L&choe=UTF-8&chl=" + adres);
    }

    public void initWebserverStuff() {
        //check files
        putFiles();
        //https://github.com/NanoHttpd/nanohttpd
        MyServer.main(this);
    }

    public void putFiles() {
        File webDir = new File("/sdcard/FrankkieOuyaLauncher/web/");
        if (!webDir.exists()) {
            webDir.mkdirs();
            File webJsDir = new File("/sdcard/FrankkieOuyaLauncher/web/js/");
            File webCssDir = new File("/sdcard/FrankkieOuyaLauncher/web/css/");
            webJsDir.mkdirs();
            webCssDir.mkdirs();
            try {
                AssetManager asm = getAssets();
                String[] l = asm.list("");
                String[] m = asm.list("webkit");
                String[] list = asm.list("web/js");
                for (String s : list) {
                    InputStream in = asm.open("web/js/" + s);
                    try {
                        Util.copyAsset(in, new File("/sdcard/FrankkieOuyaLauncher/web/js/" + s));
                    } catch (Exception e) {
                        //
                        e.printStackTrace();
                    }
                }
                String[] list2 = asm.list("web/css");
                for (String s : list2) {
                    InputStream in = asm.open("web/css/" + s);
                    try {
                        Util.copyAsset(in, new File("/sdcard/FrankkieOuyaLauncher/web/css/" + s));
                    } catch (Exception e) {
                        //
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.webserver);
        //Util.setBackground(this);
        Util.setLogo(this);
        tv1 = (TextView) findViewById(R.id.webserver_tv1);
        tv2 = (TextView) findViewById(R.id.webserver_tv2);
        tv2sb = new StringBuilder();
        log("Begin Log");
        String template = "Please use our browser on your PC and go to:\nhttp://%s:1234/";
        tv1.setText(String.format(template, getIPAddress(true)));
    }

    public void log(final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                logHandler(s);
            }
        });
    }

    public void logHandler(String s) {
        Log.e("BAXY", s);
        tv2sb.insert(0, s + "\n");
        tv2.setText(tv2sb.toString());
        tv2.invalidate();
        tv2.postInvalidate();
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     * http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
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

    public static final String uploadHtml = "\n" +
            "<form enctype=\"multipart/form-data\" action=\"/upload/\" method=\"post\">\n" +
            "<input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"2000000\">\n" +
            "File: <input name=\"uploadFile\" type=\"file\"><br>\n" +
            "Path: <input type=\"text\" name=\"pad\" value=\"/sdcard/\"><br>\n" +
            "<input name=\"gezien\" value=\"ja\" type=\"hidden\">\n" +
            "<input type=\"submit\" value=\"Start Upload\" name=\"submitButton\">\n" +
            "</form>\n";
}
