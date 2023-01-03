package com.beloncode.hackinarm;

import android.os.ParcelFileDescriptor;
import android.net.Uri;

import java.io.FileDescriptor;
import java.io.FileInputStream;

public class IpaObjectFront {
    public Uri m_ipa_uri;
    public String m_ipa_filename;
    public FileDescriptor m_descriptor;
    public ParcelFileDescriptor m_parser_fd;
    public FileInputStream m_stream;
}
