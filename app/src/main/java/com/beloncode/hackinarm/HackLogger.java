package com.beloncode.hackinarm;

import android.util.Log;

public class HackLogger {
    private final int pf_default_log_max;
    private final String pf_logger_tag = "HackinARM frontend";

    HackLogger(int defaultLog) {
        pf_default_log_max = defaultLog;
    }

    void release(@SuppressWarnings("SameParameterValue") final String frontEndMessage) {
        switch(pf_default_log_max) {
            case Log.INFO:
                Log.d(pf_logger_tag, frontEndMessage);
                break;
            case Log.DEBUG:
            case Log.VERBOSE:
            case Log.WARN:
            case Log.ERROR:
        }
    }
    void release(int logLevel, String frontEndMessage) {
        switch (logLevel) {
            case Log.ERROR: case Log.WARN:
                Log.e(pf_logger_tag, frontEndMessage);
            case Log.DEBUG:
        }
    }
}
