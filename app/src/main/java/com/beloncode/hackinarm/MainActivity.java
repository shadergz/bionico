package com.beloncode.hackinarm;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.beloncode.hackinarm.adapter.IpaAdapter;
import com.beloncode.hackinarm.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    final int f_viewerList = R.drawable.ic_baseline_view_list_24;
    final int f_gridList = R.drawable.ic_baseline_grid_view_24;

    int m_list_icon = f_viewerList;
    private HackLogger p_main_logger = null;
    private IpaHandler p_main_ipa_handler = null;
    private IpaAdapter main_ipa_adapter = null;

    ActivityResultLauncher<Intent> get_provider_result = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null || result.getResultCode() != RESULT_OK) {
                    p_main_logger.releaseMessage(HackLogger.ERROR_LEVEL,
                            "Can't open the file or no file has been selected",
                            true);
                    return;
                }
                Uri file_uri = result.getData().getData();
                String absolute_ipa_path = "(Undefined)";

                try {
                    if (!p_main_ipa_handler.handleNewIpa(file_uri)) {
                        // We can't add this element inside our list
                        return;
                    }
                    absolute_ipa_path = p_main_ipa_handler.getIpaFilename(file_uri);
                } catch (IpaException ipa_exception) {
                    p_main_logger.releaseMessage(HackLogger.WARN_LEVEL, ipa_exception.getMessage());
                }
                // Placing new object into the list
                assert absolute_ipa_path != null;
                IpaObjectFront object_ipa = p_main_ipa_handler.getIpaObject(absolute_ipa_path);
                main_ipa_adapter.placeNewItem(object_ipa);

                final String toast_message = String.format("Adding an IPA package with pathname: %s",
                        absolute_ipa_path);
                p_main_logger.releaseMessage(HackLogger.USER_LEVEL, toast_message, true);
            });

    public void selectIpaFile() {
        Intent open_document_provider = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        open_document_provider.setType("*/*");
        open_document_provider.addCategory(Intent.CATEGORY_OPENABLE);

        get_provider_result.launch(open_document_provider);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        p_main_logger = new HackLogger(HackLogger.DEBUG_LEVEL, getApplicationContext());
        p_main_ipa_handler = new IpaHandler(getApplicationContext(), p_main_logger);

        hackInitSystem();
        NavigationBarView main_bar_view = findViewById(R.id.nav_screen);
        main_bar_view.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.view_button) {
                Drawable list_icon = ResourcesCompat.getDrawable(getResources(), f_viewerList,
                        getTheme());
                Drawable grid_icon = ResourcesCompat.getDrawable(getResources(), f_gridList,
                        getTheme());
                if (m_list_icon == f_viewerList) {
                    item.setIcon(grid_icon);
                    m_list_icon = f_gridList;
                } else {
                    item.setIcon(list_icon);
                    m_list_icon = f_viewerList;
                }
            }
            return true;
        });

        FloatingActionButton main_add_button = findViewById(R.id.add_button);
        main_add_button.setOnClickListener(view -> selectIpaFile());

        final Context main_context = getApplicationContext();
        RecyclerView.LayoutManager main_context_list_layout = new LinearLayoutManager(main_context);
        main_ipa_adapter = new IpaAdapter();

        final RecyclerView main_ipa_list = findViewById(R.id.ipa_list);
        main_ipa_list.setLayoutManager(main_context_list_layout);
        main_ipa_list.setAdapter(main_ipa_adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hackPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hackResume();

        /* Requesting the permissions needed by the simulator */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
        {
            final String read_external_per = Manifest.permission.READ_EXTERNAL_STORAGE;
            final String write_external_per = Manifest.permission.WRITE_EXTERNAL_STORAGE;

            if ((ContextCompat.checkSelfPermission(getApplicationContext(), read_external_per)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(getApplicationContext(), write_external_per)
                    != PackageManager.PERMISSION_GRANTED))
            {
                final String[] request_permissions = { read_external_per, write_external_per };
                ActivityCompat.requestPermissions(this, request_permissions, 1);
            }

        } else {
            Intent access_perm_intent = new Intent(
                    Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            if (!Environment.isExternalStorageEmulated()) {
                startActivityIfNeeded(access_perm_intent, 1);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hackDestroy();
        get_provider_result.unregister();
        try {
            // Destroying all IO controllable resources
            p_main_ipa_handler.invalidateAllResources();
        } catch (IpaException ipaException) {
            p_main_logger.releaseMessage(HackLogger.ERROR_LEVEL, ipaException.getMessage());
        }
    }

    public native boolean hackInitSystem();
    public native boolean hackPause();
    public native boolean hackResume();
    public native boolean hackDestroy();

    static {
        System.loadLibrary("hackback");
    }

}