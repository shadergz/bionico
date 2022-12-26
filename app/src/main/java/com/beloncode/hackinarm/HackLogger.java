package com.beloncode.hackinarm;

import android.util.Log;

public class HackLogger {
    private final int defaultLogMax;
    private final String loggerTag = "HackinARM frontend";

    HackLogger(int defaultLog) {
        defaultLogMax = defaultLog;
    }

    void release(@SuppressWarnings("SameParameterValue") final String frontEndMessage) {
        switch(defaultLogMax) {
            case Log.INFO:
                Log.d(loggerTag, frontEndMessage);
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
                Log.e(loggerTag, frontEndMessage);
            case Log.DEBUG:
        }
    }
}
