package nl.wotuu.database;

/**
 * Created by Wouter on 6/27/13.
 */

import android.util.Log;

import java.util.Arrays;

public class Logger {

    private static final String LOG_TAG = "getrainer";

    /**
     * @see <a href="http://stackoverflow.com/a/8899735" />
     */
    private static final int ENTRY_MAX_LEN = 4000;

    /**
     * @param args If the last argument is an exception than it prints out the stack trace, and there should be no {}
     *             or %s placeholder for it.
     */
    public static void d(String message, Object... args) {
        log(Log.DEBUG, true, message, args);
    }

    /**
     * Display the entire message, showing multiple lines if there are over 4000 characters rather than truncating it.
     */
    public static void debugEntire(String message, Object... args) {
        log(Log.DEBUG, true, message, args);
    }

    public static void i(String message, Object... args) {
        log(Log.INFO, true, message, args);
    }

    public static void w(String message, Object... args) {
        log(Log.WARN, true, message, args);
    }

    public static void e(String message, Object... args) {
        log(Log.ERROR, true, message, args);
    }

    private static void log(int priority, boolean ignoreLimit, String message, Object... args) {
        String print;
        if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable) {
            Object[] truncated = Arrays.copyOf(args, args.length - 1);
            Throwable ex = (Throwable) args[args.length - 1];
            print = formatMessage(message, truncated) + '\n' + Log.getStackTraceString(ex);
        } else {
            print = formatMessage(message, args);
        }
        if (ignoreLimit) {
            while (!print.isEmpty()) {
                int lastNewLine = print.lastIndexOf('\n', ENTRY_MAX_LEN);
                int nextEnd = lastNewLine != -1 ? lastNewLine : Math.min(ENTRY_MAX_LEN, print.length());
                String next = print.substring(0, nextEnd /*exclusive*/);
                Log.println(priority, LOG_TAG, next);
                if (lastNewLine != -1) {
                    // Don't print out the \n twice.
                    print = print.substring(nextEnd + 1);
                } else {
                    print = print.substring(nextEnd);
                }
            }
        } else {
            Log.println(priority, LOG_TAG, print);
        }
    }

    private static String formatMessage(String message, Object... args) {
        String formatted;
        try {
            /*
             * {} is used by SLF4J so keep it compatible with that as it's easy to forget to use %s when you are
             * switching back and forth between server and client code.
             */
            formatted = String.format(message.replaceAll("\\{\\}", "%s"), args);
        } catch (Exception ex) {
            formatted = message + Arrays.toString(args);
        }
        return formatted;
    }
}