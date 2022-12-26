package com.beloncode.hackinarm;

import android.util.Log;

public class HackLogger {
    private final int pf_defaultLogMax;
    private final String pf_loggerTag = "HackinARM frontend";

    HackLogger(int defaultLog) {
        pf_defaultLogMax = defaultLog;
    }

    void release(@SuppressWarnings("SameParameterValue") final String frontEndMessage) {
        switch(pf_defaultLogMax) {
            case Log.INFO:
                Log.d(pf_loggerTag, frontEndMessage);
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
                Log.e(pf_loggerTag, frontEndMessage);
            case Log.DEBUG:
        }
    }
}
