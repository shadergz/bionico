package com.beloncode.akane;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

// This helper will help us to manager the openable database resource
public class StorageDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_FILEPATH = AppDBContract.APP_DB_FILEPATH;

    public StorageDBHelper(Context context) {
        super(context, DATABASE_FILEPATH, null, DATABASE_VERSION);

        defUndefinedValues.put(AppDBContract.StorageContent.COL_FILEPATH_EXT_DB,
                ExternalDBContract.EXT_DATABASE_FILEPATH);

        defUndefinedValues.put(AppDBContract.StorageContent.COL_FILEPATH_EXT_DIR, "Undefined");
        defUndefinedValues.put(AppDBContract.StorageContent.COL_NAME_EXT_DB_ENCODE, "Undefined");
        defUndefinedValues.put(AppDBContract.StorageContent.COL_NAME_EXT_DB_CHECKSUM,
                "Non-calculated");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppDBContract.SQL_QUERY_CREATE);

        initializeUserDb(db);
    }

    public final Map<String, String> defUndefinedValues = new HashMap<>();

    public void initializeUserDb(final SQLiteDatabase dbWritable) {
        final ContentValues dbInitialValues = new ContentValues();

        // Setting up the external database file path, we will query later and set-ups others
        // information after!
        for (Map.Entry<String, String> defEntry : defUndefinedValues.entrySet()) {
            dbInitialValues.put(defEntry.getKey(), defEntry.getValue());
        }

        final String tableName = AppDBContract.StorageContent.TABLE_STORAGE_NAME;
        dbWritable.insert(tableName, null, dbInitialValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
