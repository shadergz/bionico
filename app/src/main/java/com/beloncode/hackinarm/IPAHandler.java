package com.beloncode.hackinarm;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

class IpaException extends Exception {
    public IpaException(String exceptionMessage) {
        super(exceptionMessage);
    }
}

public class IPAHandler {

    private final Context pf_main_context;
    private final HackLogger pf_logger;

    IPAHandler(Context applicationContext, HackLogger logger) {
        pf_logger = logger;
        pf_main_context = applicationContext;
        m_ipa_list = new ArrayList<>();
    }

    private final ArrayList<IpaObjectFront> m_ipa_list;

    final IpaObjectFront getIpaObject(@NonNull String ipa_filename) {
        for (IpaObjectFront ipa_item : m_ipa_list) {
            if (!ipa_item.m_ipa_filename.equals(ipa_filename)) continue;

            return ipa_item;
        }
        return null;
    }
    final IpaObjectFront getIpaObject(@NonNull Uri ipa_uri) {
        for (IpaObjectFront ipa_item : m_ipa_list) {
            if (!ipa_item.m_ipa_uri.equals(ipa_uri)) continue;

            return ipa_item;
        }
        return null;
    }

    final String getIpaFilename(@NonNull Uri ipa_uri) {
        final IpaObjectFront ipa_object = getIpaObject(ipa_uri);

        if (ipa_object == null) return null;

        return ipa_object.m_ipa_filename;
    }

    private boolean verifyForIpaOccurrence(final Uri ipa_uri) {
        return getIpaObject(ipa_uri) != null;
    }

    // Translating an URI document into an absolute path name
    public boolean handleNewIpa(@NonNull Uri ipa_uri) throws IpaException {

        if (verifyForIpaOccurrence(ipa_uri)) {
            // IPA is already inside our list, we can't added one more time!
            final String error_str = String.format("Object with URI %s already inside our list!",
                    ipa_uri);
            pf_logger.release(Log.ERROR, error_str);
            return false;
        }
        final IpaObjectFront local_item = new IpaObjectFront();

        final ContentResolver f_main_resolver = pf_main_context.getContentResolver();
        // File descriptor by himself
        final FileDescriptor f_file_desc;

        try {
            // Opening the file for read and save his file descriptor inside the fd resolver!
            local_item.m_parser_fd = f_main_resolver.openFileDescriptor(ipa_uri, "r");
            local_item.m_ipa_uri = ipa_uri;

            ParcelFileDescriptor ipa_parser = local_item.m_parser_fd;
            f_file_desc = ipa_parser.getFileDescriptor();

            if (!f_file_desc.valid()) {
                final String fd_exception = String.format("File descriptor for %s not valid!",
                        ipa_uri);
                throw new IpaException(fd_exception);
            }
        } catch (IOException e) {
            final String io_error_cause = String.format("IPA filename can't be located, because %s",
                    e.getMessage());
            throw new IpaException(io_error_cause);
        }

        local_item.m_descriptor = f_file_desc;

        final int last_readable_length = m_ipa_list.size();
        final int current_readable_length = openIPAStreamBuffer(local_item);
        assert last_readable_length != current_readable_length:
                "No one readable content added into list";
        // Returning it's filename
        return true;
    }

    private int openIPAStreamBuffer(IpaObjectFront ipa_file) throws IpaException {

        FileDescriptor fd_object = ipa_file.m_descriptor;
        assert fd_object.valid() : "File descriptor must be valid for this operation";

        final FileInputStream input_stream = new FileInputStream(fd_object);
        final BufferedInputStream buffer_reader = new BufferedInputStream(input_stream);

        final byte[] local_buffer = new byte[10];
        try {
            // Testing whether we can properly read the file
            if ((buffer_reader.read(local_buffer, 0, local_buffer.length)) == -1) {
                throw new IpaException("The file is no longer available for our use!");
            }
            // Resenting internal file cursor
            //buffer_reader.reset();
        } catch (IOException io_exception) {
            final String io_except = String.format("Input/Output Exception caused by %s",
                    io_exception.getMessage());
            throw new IpaException(io_except);
        }
        // Now that the FileDescriptor is ready and it's valid, we can use it into Backend
        ipa_file.m_stream = input_stream;

        ipa_file.m_ipa_filename = hackPushIpaFile(ipa_file);
        if (ipa_file.m_ipa_filename == null) {
            throw new IpaException("The IPA package filename couldn't be fetched");
        }

        m_ipa_list.add(ipa_file);
        return m_ipa_list.size();
    }

    void invalidateAllResources() throws IpaException {
        try {
            for (IpaObjectFront ipa_item : m_ipa_list) {
                // Feeding all resources used to handler an IPA Item
                final FileInputStream file_input = ipa_item.m_stream;
                ParcelFileDescriptor context_parser = ipa_item.m_parser_fd;
                @SuppressLint("DefaultLocale") final String log_rela = String.format(
                        "Destroying relationship with file descriptor %d\n", context_parser.getFd());
                pf_logger.release(log_rela);
                final int ipa_fd = context_parser.getFd();
                assert hackPopIpaFile(ipa_fd);

                file_input.close();
                if (context_parser.getFileDescriptor().valid())
                    context_parser.close();
                m_ipa_list.remove(ipa_item);
            }
        } catch (IOException io_exception) {
            final String caught_problem = String.format("Input/Output exception generated because %s",
                    io_exception.getMessage());

            throw new IpaException(caught_problem);
        }
    }

    static native String hackPushIpaFile(IpaObjectFront ipaItem);
    static native boolean hackPopIpaFile(int ipa_file_descriptor);

}
