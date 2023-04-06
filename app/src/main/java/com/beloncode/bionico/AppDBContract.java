package com.beloncode.bionico;

import android.provider.BaseColumns;

public final class AppDBContract {
    private AppDBContract() {
    }

    public static final String APP_DB_FILEPATH = "User.db";

    public static class StorageContent implements BaseColumns {
        public static final String TABLE_STORAGE_NAME = "Storage";

        public static final String COL_FILEPATH_EXT_DIR = "ExternalDirectory";
        public static final String COL_FILEPATH_EXT_DB = "DatabaseDirectory";
        public static final String COL_NAME_EXT_DB_ENCODE = "EncodeType";
        public static final String COL_NAME_EXT_DB_CHECKSUM = "Checksum";
    }

    public static final String SQL_QUERY_CREATE = String.format(
            "CREATE TABLE \"%s\" (\"%s\" INTEGER PRIMARY KEY, \"%s\" TEXT, \"%s\" TEXT, \"%s\" TEXT, \"%s\" TEXT)",
            StorageContent.TABLE_STORAGE_NAME, StorageContent._ID, StorageContent.COL_FILEPATH_EXT_DIR,
            StorageContent.COL_FILEPATH_EXT_DB, StorageContent.COL_NAME_EXT_DB_ENCODE,
            StorageContent.COL_NAME_EXT_DB_CHECKSUM
    );
}

