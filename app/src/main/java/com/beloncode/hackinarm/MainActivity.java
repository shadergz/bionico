package com.beloncode.hackinarm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beloncode.hackinarm.adapter.IpaAdapter;
import com.beloncode.hackinarm.databinding.ActivityMainBinding;
import com.beloncode.hackinarm.ipa.IpaInstaller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final int viewerList = R.drawable.ic_baseline_view_list_24;
    private final int gridList = R.drawable.ic_baseline_grid_view_24;

    private int listIcon = viewerList;
    private HackLogger mainLogger = null;
    private IpaInstaller mainCoreInstaller = null;
    private IpaHandler mainIpaHandler = null;
    private IpaAdapter mainIpaAdapter = null;
    private Storage mainStorage = null;

    private ActivityResultLauncher<Intent> getExternalDirectory;
    private ActivityResultLauncher<Intent> getIpaFromContent;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMainRes();
        setupActivitiesResults();
        engineInitSystem();
        mainLogger = new HackLogger(this, HackLogger.DEBUG_LEVEL);

        mainIpaHandler = new IpaHandler(this);
        mainCoreInstaller = new IpaInstaller();

        mainStorage = new Storage(this);
    }

    private void setupActivitiesResults() {
        getExternalDirectory = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() == null || result.getResultCode() != RESULT_OK) {
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL,
                        "Can't open the desired folder, or no one is specified", true);
                return;
            }
            Uri dirUri = result.getData().getData();
            mainStorage.setExternal(dirUri);

            try {
                if (!mainStorage.checkoutDirectories(mainStorage.getExternal())) {
                    // Create all directories if they not exist yet!
                    mainStorage.createMainDirectories(mainStorage.getExternal());
                }
            } catch (final IOException ioExcept) {
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, ioExcept.getMessage());
                return;
            }
            mainStorage.saveExternalStoragePath(mainStorage.getExternalPath());
            mainStorage.setupExternalStorage();
        });

        getIpaFromContent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() == null || result.getResultCode() != RESULT_OK) {
                final String errorOne = "Can't open the file or no file has been selected";
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, errorOne, true);
                return;
            }
            Intent ipaIntent = result.getData();
            IpaObject newIpaObject = new IpaObject(ipaIntent);

            final File ipaFile = newIpaObject.getRegularFile();

            if (!ipaFile.isFile() || !ipaFile.canRead()) {
                final String cantRead = String.format("Can't read file (%s), isn't a regular file!",
                        ipaFile.getAbsolutePath());
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, cantRead);
                return;
            }

            try {
                mainIpaHandler.handleNewIpa(newIpaObject);
                mainCoreInstaller.installNewIpa(newIpaObject);

            } catch (IpaException ipaException) {
                final String ipaError = String.format("Error occurred while tried to handler a " +
                        "new Ipa object, with file path: %s", ipaFile.getAbsolutePath());
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, ipaError);
                return;
            }

            mainIpaAdapter.placeNewItem(newIpaObject);
            final String toastMessage = String.format("Adding an Ipa package with filename: %s",
                    newIpaObject.ipaFilename);
            mainLogger.releaseMessage(HackLogger.USER_LEVEL, toastMessage, true);
        });
    }

    private void setupMainRes() {

        final NavigationBarView mainBarView = findViewById(R.id.nav_screen);
        mainBarView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.view_button) {
                Drawable listIcon = ResourcesCompat.getDrawable(getResources(), viewerList,
                        getTheme());
                Drawable gridIcon = ResourcesCompat.getDrawable(getResources(), gridList,
                        getTheme());
                if (this.listIcon == viewerList) {
                    item.setIcon(gridIcon);
                    this.listIcon = gridList;
                } else {
                    item.setIcon(listIcon);
                    this.listIcon = viewerList;
                }
            }
            return true;
        });

        final FloatingActionButton mainAddButton = findViewById(R.id.add_button);
        mainAddButton.setOnClickListener(view -> selectIpaFile());

        final Context mainContext = getApplicationContext();
        final RecyclerView.LayoutManager mainCtxListLayout = new LinearLayoutManager(mainContext);
        mainIpaAdapter = new IpaAdapter();

        final RecyclerView mainIpaList = findViewById(R.id.ipa_list);
        mainIpaList.setLayoutManager(mainCtxListLayout);
        mainIpaList.setAdapter(mainIpaAdapter);
    }

    public HackLogger getLogger() {
        return mainLogger;
    }

    public void requestExternalStorage() {
        Intent openExtProvider = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        getExternalDirectory.launch(openExtProvider);
    }

    public void selectIpaFile() {
        Intent openDocumentProvider = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        openDocumentProvider.setType("*/*");
        openDocumentProvider.addCategory(Intent.CATEGORY_OPENABLE);

        getIpaFromContent.launch(openDocumentProvider);
    }

    public void acquireStorageAccessPolicy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            final String readExtPer = Manifest.permission.READ_EXTERNAL_STORAGE;
            final String writeExtPer = Manifest.permission.WRITE_EXTERNAL_STORAGE;

            if ((ContextCompat.checkSelfPermission(getApplicationContext(), readExtPer)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(getApplicationContext(), writeExtPer)
                            != PackageManager.PERMISSION_GRANTED)) {
                final String[] requestPermissions = {readExtPer, writeExtPer};
                ActivityCompat.requestPermissions(this, requestPermissions, 1);
            }

        } else {
            Intent accessPermIntent = new Intent(
                    Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            if (!Environment.isExternalStorageEmulated()) {
                startActivityIfNeeded(accessPermIntent, 1);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        enginePause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        engineResume();
        /* Requesting the permissions needed by the simulator */
        acquireStorageAccessPolicy();
        mainStorage.getExternalStorageAccess();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getExternalDirectory.unregister();
        getIpaFromContent.unregister();

        engineDestroy();
        if (mainStorage != null) mainStorage.release();
        try {
            // Destroying all IO controllable resources
            mainIpaHandler.invalidateAllResources();
        } catch (IpaException ipaException) {
            mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, ipaException.getMessage());
        }
    }

    public native boolean engineInitSystem();

    public native boolean enginePause();

    public native boolean engineResume();

    public native boolean engineDestroy();

    static {
        System.loadLibrary("hackback");
    }

}