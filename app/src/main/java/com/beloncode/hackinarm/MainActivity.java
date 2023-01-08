package com.beloncode.hackinarm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

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

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    final int viewerList = R.drawable.ic_baseline_view_list_24;
    final int gridList = R.drawable.ic_baseline_grid_view_24;

    int listIcon = viewerList;
    public HackLogger mainLogger = null;
    public IpaInstaller mainCoreInstaller = null;
    public IpaHandler mainIpaHandler = null;
    public IpaAdapter mainIpaAdapter = null;
    public Storage mainStorage = null;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        engineInitSystem();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainLogger = new HackLogger(HackLogger.DEBUG_LEVEL);

        mainIpaHandler = new IpaHandler();
        mainCoreInstaller = new IpaInstaller();

        try {
            mainStorage = new Storage(getApplicationContext());
        } catch (FileNotFoundException eFileNotFound) {
            eFileNotFound.printStackTrace();
        }

        NavigationBarView mainBarView = findViewById(R.id.nav_screen);
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
        mainAddButton.setOnClickListener(view -> mainIpaHandler.selectIpaFile());

        final Context mainContext = getApplicationContext();
        final RecyclerView.LayoutManager mainCtxListLayout = new LinearLayoutManager(mainContext);
        mainIpaAdapter = new IpaAdapter();

        final RecyclerView mainIpaList = findViewById(R.id.ipa_list);
        mainIpaList.setLayoutManager(mainCtxListLayout);
        mainIpaList.setAdapter(mainIpaAdapter);
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
    protected void onDestroy() {
        super.onDestroy();
        engineDestroy();

        if (mainStorage != null)
            mainStorage.release();

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