package com.beloncode.hackinarm;

import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;

public class IPAItemFront {
    public String m_IPAFilename;
    public FileDescriptor m_FileDescriptor;
    public ParcelFileDescriptor m_ParserFD;
    public FileInputStream m_FileInputStream;
}
