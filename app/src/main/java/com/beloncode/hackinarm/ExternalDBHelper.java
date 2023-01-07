package com.beloncode.hackinarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExternalDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public ExternalDBHelper(Context context, String dbExternalFilepath) {
        super(context, dbExternalFilepath, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ExternalDBContract.SQL_QUERY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
