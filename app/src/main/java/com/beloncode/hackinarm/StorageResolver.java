package com.beloncode.hackinarm;

/*
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
*/

public class StorageResolver extends MainActivity {
    StorageResolver() {
    }
    // Translating a regular Uri into a Path, normally used to translate a Uri returned by the content
    // provider into a correct file path, without handles a file descriptor associated with the file!

    /*
    public final String getFilenameUri(final Uri regularUri) {
        if (regularUri == null) return null;

        if (regularUri.getScheme().equals("content")) {
            Cursor contentCursor = getContentResolver().query(regularUri, null, null, null, null);

            if (contentCursor == null) return null;
            contentCursor.moveToFirst();

            final String dspFilePath = contentCursor.getString(
                    contentCursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            );

            contentCursor.close();
            return dspFilePath;
        }

        String filePath = regularUri.getPath();
        final int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash != -1)
            filePath = filePath.substring(filePath.lastIndexOf(lastSlash + 1));
        return filePath;
    }
    */
}
