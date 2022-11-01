package com.beloncode.hackinarm;

import android.util.Log;

public class HackLogger {
    private final int defaultLogMax;

    HackLogger(int defaultLog) {
        defaultLogMax = defaultLog;
    }

    void release(@SuppressWarnings("SameParameterValue") String frontEndMessage) {
        final String loggerTag = "HackinARM Frontend";
        switch(defaultLogMax) {
            case Log.INFO:
                Log.d(loggerTag, frontEndMessage);
                break;
            case Log.DEBUG:
            case Log.VERBOSE:
            case Log.WARN:
        }
    }
}
