package com.beloncode.hackinarm;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class HackLogger {

    public final static int DEBUG_LEVEL = 0;
    public final static int VERBOSE_LEVEL = 1;
    public final static int WARN_LEVEL = 2;
    public final static int ERROR_LEVEL = 3;
    public final static int USER_LEVEL = 4;
    public final static int INFO_LEVEL = 5;

    private final int pf_default_log_max;
    private final Context p_main_context;

    HackLogger(int defaultLog, Context main) {
        p_main_context = main;
        pf_default_log_max = defaultLog;
    }

    static String pf_logger_tag = "HackinARM frontend";

    void detachMessage(final int logging, final String front_msg, boolean advise_user) {
        switch(logging) {
            case DEBUG_LEVEL: Log.d(pf_logger_tag, front_msg); break;
            case VERBOSE_LEVEL:
            case WARN_LEVEL:
            case ERROR_LEVEL: Log.e(pf_logger_tag, front_msg); break;
            case USER_LEVEL:  Log.i(pf_logger_tag, front_msg); break;
            case INFO_LEVEL:
        }
        if (advise_user) {
            Toast.makeText(p_main_context, front_msg, Toast.LENGTH_SHORT).show();
        }
    }

    void releaseMessage(final int log_level, final String message) {
        detachMessage(log_level, message, false);
    }
    void releaseMessage(final int log_level, final String message, final boolean user_toast) {
        detachMessage(log_level, message, user_toast);
    }
    void releaseMessage(final String message) {
        detachMessage(pf_default_log_max, message, false);
    }
}
