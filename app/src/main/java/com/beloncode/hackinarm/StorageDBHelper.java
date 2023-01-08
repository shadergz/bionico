package com.beloncode.hackinarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// This helper will help us to manager the openable database resource
public class StorageDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_FILEPATH = AppDBContract.APP_DB_FILEPATH;

    public StorageDBHelper(Context context) {
        super(context, DATABASE_FILEPATH, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppDBContract.SQL_QUERY_CREATE);

        ContentValues dbInitialValues = new ContentValues();

        // Setting up the external database file path, we will query later and set-ups others
        // information after!
        final String dbPath = AppDBContract.StorageContent.COL_FILEPATH_EXT_DB;

        dbInitialValues.put(dbPath, ExternalDBContract.EXT_DATABASE_FILEPATH);

        final String tableName = AppDBContract.StorageContent.TABLE_STORAGE_NAME;

        db.insert(tableName, null, dbInitialValues);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
