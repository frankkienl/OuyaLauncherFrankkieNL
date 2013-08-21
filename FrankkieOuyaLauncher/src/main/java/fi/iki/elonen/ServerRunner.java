package fi.iki.elonen;

import android.util.Log;

import java.io.IOException;

public class ServerRunner {
    public static void run(Class serverClass) {
        try {
            executeInstance((NanoHTTPD) serverClass.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executeInstance(NanoHTTPD server) {
        try {
            server.start();
        } catch (IOException ioe) {
            Log.e("BAXY", "Couldn't start server:\n" + ioe);
            //System.exit(-1);
        }

//        Log.e("BAXY", ("Server started, Hit Enter to stop.\n");
//
//        try {
//            System.in.read();
//        } catch (Throwable ignored) {
//        }
//
//        server.stop();
//        Log.e("BAXY", ("Server stopped.\n");
    }
}
