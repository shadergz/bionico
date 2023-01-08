package com.beloncode.hackinarm;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

public class Storage {

    // Saving non dynamic resources values, so on we can avoid queries call and work as
    // ahead of time!
    ArrayList<String> queriedPaths = new ArrayList<>();
    private final SQLiteDatabase dbSystemR;
    private final SQLiteDatabase dbSystemW;

    private final StorageDBHelper dbSystemRes;
    private final ExternalDBHelper dbExternRes;

    MainActivity mainContext;
    ActivityResultLauncher<Intent> selectExternalDir;

    private File currentExternalDir;
    private File[] filesList;
    private final String[] addonRequiredDirs = new String[] {
            "Storage", "System"
    };

    boolean isExternalDirDefined() {
        final Map<String, String> defValues = dbSystemRes.defUndefinedValues;

        return !defValues.containsValue(queriedPaths.get(getStorageIndex(
                StoragePathIndexes.STORAGE_EXT_DIR)));
    }

    public Storage(final MainActivity mainActivity, ActivityResultLauncher<Intent> lambOpenExternalDir) throws FileNotFoundException {

        dbSystemRes = new StorageDBHelper(mainActivity.getApplicationContext());

        dbSystemR = dbSystemRes.getReadableDatabase();
        dbSystemW = dbSystemRes.getWritableDatabase();
        mainContext = mainActivity;
        selectExternalDir = lambOpenExternalDir;

        assert updateExternalPaths();

        if (!isExternalDirDefined()) {
            requestExternalStorage();
            updateExternalPaths();
        }

        if (!currentExternalDir.exists()) {
            final String errorProblem = String.format("Can't read the main external directory in %s",
                    currentExternalDir.getAbsolutePath());
            mainContext.getLogger().releaseMessage(HackLogger.ERROR_LEVEL, errorProblem, true);
            throw new FileNotFoundException();
        }

        final String dbAbsolutePath = String.format("%s/%s",
                queriedPaths.get(getStorageIndex(StoragePathIndexes.STORAGE_EXT_DIR)),
                queriedPaths.get(getStorageIndex(StoragePathIndexes.STORAGE_EXT_DATABASE_PATH))
        );

        dbExternRes = new ExternalDBHelper(mainActivity, dbAbsolutePath);
    }

    @NonNull
    public final String getExternalPath() {
        return currentExternalDir.getAbsolutePath();
    }

    public final File getExternal() {
        return currentExternalDir;
    }

    public void setExternal(@NonNull final File newExtDir) {
        if (!newExtDir.exists() && newExtDir.isFile()) return;
        currentExternalDir = newExtDir;
    }

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
                        "%s, as a subdirectory of %s", regFolder, folder.getAbsolutePath());
                mainContext.getLogger().releaseMessage(HackLogger.ERROR_LEVEL, errorString, true);
            }
        }
    }

    void requestExternalStorage() {
        Intent openExtProvider = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        openExtProvider.setType("*/*");
        openExtProvider.addCategory(Intent.CATEGORY_OPENABLE);

        selectExternalDir.launch(openExtProvider);
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
        if (dbSystemR.isOpen()) dbSystemR.close();
        if (dbSystemW.isOpen()) dbSystemW.close();

        dbSystemRes.close();
        dbExternRes.close();
    }

    enum StoragePathIndexes {
        STORAGE_EXT_DIR,
        STORAGE_EXT_DATABASE_PATH
    }

    final int getStorageIndex(StoragePathIndexes index) {
        return index.ordinal();
    }

    private final String[] firstRow = new String[]{
            "1"
    };

    final boolean updateExternalPaths() {
        final String filepathDir = AppDBContract.StorageContent.COL_FILEPATH_EXT_DIR;
        final String dbPathDir = AppDBContract.StorageContent.COL_FILEPATH_EXT_DB;
        final String targetTable = AppDBContract.StorageContent.TABLE_STORAGE_NAME;

        final String[] columnName = new String[]{filepathDir, dbPathDir};
        Cursor tableCursor = dbSystemR.query(targetTable, columnName,
                "_id = ?", firstRow, null, null, null);
        if (tableCursor == null) return false;

        try {
            tableCursor.moveToNext();

            queriedPaths.add(tableCursor.getString(tableCursor.getColumnIndexOrThrow(filepathDir)));
            queriedPaths.add(tableCursor.getString(tableCursor.getColumnIndexOrThrow(dbPathDir)));
        } finally {
            tableCursor.close();
        }

        return true;
    }
}
