package com.beloncode.bionico;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.documentfile.provider.DocumentFile;

public class StorageResolver {
    MainActivity activityMain;
    
    StorageResolver(MainActivity activity) {
        activityMain = activity;
    }
    // Translating a regular Uri into a Path, normally used to translate a Uri returned by the content
    // provider into a correct file path, without handles a file descriptor associated with the file!
    
    public final String getPathFromUri(final Uri regularUri) {

        if (regularUri == null) return null;
        final String uriScheme = regularUri.getScheme();
        if (uriScheme.equals("content")) {
            final DocumentFile dspName = DocumentFile.fromTreeUri(activityMain, regularUri);
            return dspName != null ? dspName.getName() : null;
        }

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
