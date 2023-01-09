package com.beloncode.hackinarm;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

public class StorageResolver {
    MainActivity activityMain;
    
    StorageResolver(MainActivity activity) {
        activityMain = activity;
    }
    // Translating a regular Uri into a Path, normally used to translate a Uri returned by the content
    // provider into a correct file path, without handles a file descriptor associated with the file!
    
    public final String getFilePathUri(final Uri regularUri) {

        if (regularUri == null) return null;
        if (!regularUri.getScheme().equals("content")) return null;

        final String[] dspProjection = new String[] {
                OpenableColumns.DISPLAY_NAME
        };

        final Cursor contentCursor = activityMain.getContentResolver().query(regularUri,
                dspProjection, null, null, null);

        if (contentCursor == null) return null;
        contentCursor.moveToFirst();

        final String dspFilePath = contentCursor.getString(
                contentCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));

        contentCursor.close();
        return dspFilePath;

    }

}
