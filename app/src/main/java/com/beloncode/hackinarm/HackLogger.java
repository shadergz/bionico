package com.beloncode.hackinarm;

import android.util.Log;

public class HackLogger {
    private final int pf_default_log_max;
    private final String pf_logger_tag = "HackinARM frontend";

    HackLogger(int defaultLog) {
        pf_default_log_max = defaultLog;
    }

    void release(@SuppressWarnings("SameParameterValue") final String front_end_msg) {
        switch(pf_default_log_max) {
            case Log.INFO:
                Log.d(pf_logger_tag, front_end_msg);
                break;
            case Log.DEBUG:
            case Log.VERBOSE:
            case Log.WARN:
            case Log.ERROR:
        }
    }
    void release(int logLevel, String front_end_msg) {
        switch (logLevel) {
            case Log.ERROR: case Log.WARN:
                Log.e(pf_logger_tag, front_end_msg);
            case Log.DEBUG:
        }
    }
}
