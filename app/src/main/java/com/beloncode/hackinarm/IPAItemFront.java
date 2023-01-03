package com.beloncode.hackinarm;

import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;

public class IPAItemFront {
    public String m_ipa_filename;
    public FileDescriptor m_descriptor;
    public ParcelFileDescriptor m_parser_fd;
    public FileInputStream m_stream;
}
