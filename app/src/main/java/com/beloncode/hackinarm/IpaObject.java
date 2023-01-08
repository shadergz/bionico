package com.beloncode.hackinarm;

import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

public class IpaObject {

    public IpaObject(Intent ipaDataIntent) {

        ipaUri = ipaDataIntent.getData();
        ipaFile = new File(ipaUri.getPath());
        ipaFilename = ipaFile.getAbsolutePath();

    }

    @NonNull
    final File getRegularFile() {
        return ipaFile;
    }

    @NonNull
    final Uri getRegularUri() {
        return ipaUri;
    }

    public Uri ipaUri;
    public String ipaFilename;
    private final File ipaFile;
    public FileDescriptor fDescriptor;
    public ParcelFileDescriptor openableFParser;
    public FileInputStream readableStream;
}
