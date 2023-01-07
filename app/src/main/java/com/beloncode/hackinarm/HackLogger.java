package com.beloncode.hackinarm;

import android.util.Log;
import android.widget.Toast;

public class HackLogger extends MainActivity {

    public final static int DEBUG_LEVEL = 0;
    public final static int VERBOSE_LEVEL = 1;
    public final static int WARN_LEVEL = 2;
    public final static int ERROR_LEVEL = 3;
    public final static int USER_LEVEL = 4;
    public final static int INFO_LEVEL = 5;

    private final int defLogMax;

    HackLogger(int defaultLog) {
        defLogMax = defaultLog;
    }

    static String loggerTag = "HackinARM frontend";

    void detachMessage(final int logging, final String frontMsg, boolean adviseUser) {
        switch (logging) {
            case DEBUG_LEVEL:
                Log.d(loggerTag, frontMsg);
                break;
            case VERBOSE_LEVEL:
            case WARN_LEVEL:
            case ERROR_LEVEL:
                Log.e(loggerTag, frontMsg);
                break;
            case USER_LEVEL:
                Log.i(loggerTag, frontMsg);
                break;
            case INFO_LEVEL:
        }
        if (adviseUser) {
            Toast.makeText(getApplicationContext(), frontMsg, Toast.LENGTH_SHORT).show();
        }
    }

    void releaseMessage(final int logLevel, final String message) {
        detachMessage(logLevel, message, false);
    }

    void releaseMessage(final int logLevel, final String message, final boolean userToast) {
        detachMessage(logLevel, message, userToast);
    }

    void releaseMessage(final String message) {
        detachMessage(defLogMax, message, false);
    }
}
