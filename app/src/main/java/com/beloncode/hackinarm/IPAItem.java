package com.beloncode.hackinarm;

import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;

public class IPAItem {
    public String mIPAFilename;
    public FileDescriptor mFileDescriptor;
    public ParcelFileDescriptor mParserFD;
    public FileInputStream mFileInputStream;
}
