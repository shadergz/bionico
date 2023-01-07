package com.beloncode.hackinarm;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class Storage extends MainActivity {

    // Saving non dynamic resources values, so on we can avoid queries call and work as
    // ahead of time!
    ArrayList<String> queriedPaths = null;
    private final SQLiteDatabase dbSystemR;
    private final SQLiteDatabase dbSystemW;

    private final StorageDBHelper dbSystemRes;
    private final ExternalDBHelper dbExternRes;

    private File currentExternalDir;
    private File[] filesList;
    private final String[] addonRequiredDirs = new String[]{
            "Storage", "System"
    };

    public Storage() throws FileNotFoundException {
        dbSystemRes = new StorageDBHelper(getApplicationContext());
        dbSystemR = dbSystemRes.getReadableDatabase();
        dbSystemW = dbSystemRes.getWritableDatabase();

        ArrayList<String> externalDir = getExternalPaths();

        if (externalDir.size() != 2) {
            requestExternalStorage();
            externalDir = getExternalPaths();
        }

        if (!currentExternalDir.exists()) {
            final String errorProblem = String.format("Can't read a main external directory in %s\n",
                    currentExternalDir.getAbsolutePath());
            mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, errorProblem, true);
            throw new FileNotFoundException();
        }

        final String dbAbsolutePath = String.format("%s/%s",
                externalDir.get(StoragePathIndexes.STORAGE_EXT_DIR.ordinal()),
                externalDir.get(StoragePathIndexes.STORAGE_EXT_DATABASE_PATH.ordinal())
        );

        dbExternRes = new ExternalDBHelper(getApplicationContext(), dbAbsolutePath);
    }

    ActivityResultLauncher<Intent> getExternalDirectory = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null || result.getResultCode() != RESULT_OK) {
                    mainLogger.releaseMessage(HackLogger.ERROR_LEVEL,
                            "Can't open the file or no file has been selected",
                            true);
                    return;
                }
                Uri fileUri = result.getData().getData();
                currentExternalDir = new File(fileUri.getPath());

                try {
                    if (!checkoutDirectories(currentExternalDir)) {
                        createMainDirectories(currentExternalDir);
                    }
                } catch (IOException ioExcept) {
                    ioExcept.printStackTrace();
                }
                saveExternalStoragePath(currentExternalDir.getAbsolutePath());

            });

    boolean checkoutDirectories(File extDir) throws IOException {
        if (extDir.exists()) return false;

        filesList = extDir.listFiles();
        if (filesList == null) return false;

        final Vector<String> filesPathname = new Vector<>();

        for (final File fileItem : filesList) {
            filesPathname.add(fileItem.getCanonicalPath());
        }

        return filesPathname.containsAll(Arrays.asList(addonRequiredDirs));
    }

    void createMainDirectories(File currentExternalDir) throws IOException {
        if (filesList == null)
            throw new RuntimeException("List isn't valid, this may be fatal error");

        final ArrayList<String> filesInsideExternal = new ArrayList<>();
        for (final File fileItem : filesList) {
            filesInsideExternal.add(fileItem.getCanonicalPath());
        }
        filesInsideExternal.removeAll(Arrays.asList(addonRequiredDirs));
        makeAllDirs(currentExternalDir, filesInsideExternal);
    }

    void makeAllDirs(File folder, ArrayList<String> foldersList) {
        for (final String regFolder : foldersList) {

            final String absDirLocation = String.format("%s/%s",
                    folder.getAbsolutePath(), regFolder);

            final File newFileHandler = new File(absDirLocation);
            final boolean mkDirResult = newFileHandler.mkdir();
            if (!mkDirResult) {
                final String errorString = String.format("Can't create a directory called: " +
                        "%s, as a subdirectory of %s\n", regFolder, folder.getAbsolutePath());
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, errorString, true);
            }
        }
    }

    void requestExternalStorage() {
        Intent openExtProvider = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        openExtProvider.setType("*/*");
        openExtProvider.addCategory(Intent.CATEGORY_OPENABLE);
        getExternalDirectory.launch(openExtProvider);
    }

    void saveExternalStoragePath(final String extDirectory) {
        ContentValues externalDir = new ContentValues();

        final String extCol = AppDBContract.StorageContent.COL_FILEPATH_EXT_DIR;
        externalDir.put(extCol, extDirectory);

        final String clause = String.format("%s = ?",
                AppDBContract.StorageContent.COL_FILEPATH_EXT_DIR);
        // We must ensure that the External Storage will change only once, instead of multiple
        // times!
        final String[] emptyParameter = new String[]{""};
        dbSystemW.update(AppDBContract.StorageContent.TABLE_STORAGE_NAME, externalDir,
                clause, emptyParameter);
    }

    public void release() {
        if (dbSystemR.isOpen())
            dbSystemR.close();
        if (dbSystemW.isOpen())
            dbSystemW.close();

        dbSystemRes.close();
        dbExternRes.close();
    }

    enum StoragePathIndexes {
        STORAGE_EXT_DIR,
        STORAGE_EXT_DATABASE_PATH
    }

    final ArrayList<String> getExternalPaths() {

        if (queriedPaths.size() == 2) return queriedPaths;

        final String filepathDir = AppDBContract.StorageContent.COL_FILEPATH_EXT_DIR;
        final String dbPathDir = AppDBContract.StorageContent.COL_FILEPATH_EXT_DB;

        final String[] columnName = new String[]{filepathDir, dbPathDir};
        String targetTable = AppDBContract.StorageContent.TABLE_STORAGE_NAME;
        Cursor tableCursor = dbSystemR.query(targetTable, columnName,
                null, null, null, null, null);
        assert tableCursor != null;
        tableCursor.moveToFirst();

        final ArrayList<String> pathContents = new ArrayList<>();

        try {
            final int tableIndex = tableCursor.getColumnIndexOrThrow(targetTable);
            while (tableCursor.moveToNext()) {
                pathContents.add(tableCursor.getString(tableIndex));
            }
        } finally {
            tableCursor.close();
        }
        queriedPaths = pathContents;

        return queriedPaths;
    }
}
