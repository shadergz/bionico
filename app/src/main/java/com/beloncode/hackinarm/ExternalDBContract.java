package com.beloncode.hackinarm;

import android.provider.BaseColumns;

public final class ExternalDBContract {
    private ExternalDBContract() {
    }

    public static final String EXT_DATABASE_FILEPATH = "System/Database/Core.data";

    public static class ExternalStorageContent implements BaseColumns {
        public static final String TABLE_EXT_NAME = "IPA List";
        public static final String COL_NAME_IPA_NAME = "IPA Name";
        public static final String COL_NAME_IPA_DIRECTORY = "IPA Root";
    }

    public static final String SQL_QUERY_CREATE = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT);",
            ExternalStorageContent.TABLE_EXT_NAME, ExternalStorageContent._ID,
            ExternalStorageContent.COL_NAME_IPA_NAME,
            ExternalStorageContent.COL_NAME_IPA_DIRECTORY
    );
}

