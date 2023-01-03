package com.beloncode.hackinarm;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

class IPAException extends Exception {
    public IPAException(String exceptionMessage) {
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

    private final ArrayList<IPAItemFront> m_ipa_list;

    final IPAItemFront getIpaFromName(@NonNull String ipa_filename) {
        for (IPAItemFront ipa_item : m_ipa_list) {
            if (!Objects.equals(ipa_item.m_ipa_filename, ipa_filename)) continue;

            return ipa_item;
        }
        return null;
    }

    // Translating an URI document into an absolute path name
    final String handleNewIPAFromUri(@NonNull Uri ipa_filename) throws IPAException {

        final IPAItemFront f_local_item = new IPAItemFront();

        final ContentResolver f_main_resolver = pf_main_context.getContentResolver();
        // File descriptor by himself
        final FileDescriptor f_file_desc;

        try {
            // Opening the file for read and save his file descriptor inside the fd resolver!
            f_local_item.m_parser_fd = f_main_resolver.openFileDescriptor(ipa_filename, "r");

            ParcelFileDescriptor IPAParser = f_local_item.m_parser_fd;
            f_file_desc = IPAParser.getFileDescriptor();

            if (!f_file_desc.valid()) {
                final String fdException = String.format("File descriptor for %s not valid!",
                        ipa_filename);
                throw new IPAException(fdException);
            }
        } catch (IOException e) {
            final String ioErrorCause = String.format("IPA filename can't be located, because %s",
                    e.getMessage());
            throw new IPAException(ioErrorCause);
        }

        f_local_item.m_descriptor = f_file_desc;

        final int lastReadableLength = m_ipa_list.size();
        final int currentReadableLength = openIPAStreamBuffer(f_local_item);
        assert lastReadableLength != currentReadableLength:
                "No one readable content added into list";

        // Returning it's filename
        return f_local_item.m_ipa_filename;
    }

    private int openIPAStreamBuffer(IPAItemFront ipa_file) throws IPAException {

        FileDescriptor fd_object = ipa_file.m_descriptor;
        assert fd_object.valid() : "File descriptor must be valid for this operation";

        final FileInputStream input_stream = new FileInputStream(fd_object);
        final BufferedInputStream buffer_reader = new BufferedInputStream(input_stream);

        final byte[] localBuffer = new byte[10];
        try {
            // Testing whether we can properly read the file
            if ((buffer_reader.read(localBuffer, 0, localBuffer.length)) == -1) {
                throw new IPAException("The file is no longer available for our use!");
            }
            // Resenting internal file cursor
            //buffer_reader.reset();
        } catch (IOException ioException) {
            final String io_except = String.format("Input/Output Exception caused by %s",
                    ioException.getMessage());
            throw new IPAException(io_except);
        }
        // Now that the FileDescriptor is ready and it's valid, we can use it into Backend
        ipa_file.m_stream = input_stream;

        ipa_file.m_ipa_filename = hackPushIPAFile(ipa_file);
        if (ipa_file.m_ipa_filename == null) {
            throw new IPAException("The IPA package filename couldn't be fetched");
        }

        m_ipa_list.add(ipa_file);
        return m_ipa_list.size();
    }

    void invalidateAllResources() throws IPAException {
        try {
            for (IPAItemFront ipa_item : m_ipa_list) {
                // Feeding all resources used to handler an IPA Item
                final FileInputStream fileInputStream = ipa_item.m_stream;
                ParcelFileDescriptor contextParser = ipa_item.m_parser_fd;
                @SuppressLint("DefaultLocale") final String log_rela = String.format(
                        "Destroying relationship with file descriptor %d\n", contextParser.getFd());
                pf_logger.release(log_rela);
                final int ipa_fd = contextParser.getFd();
                assert hackPopIPAFile(ipa_fd);

                fileInputStream.close();
                if (contextParser.getFileDescriptor().valid())
                    contextParser.close();
                m_ipa_list.remove(ipa_item);
            }
        } catch (IOException io_exception) {
            final String caughtProblem = String.format("Input/Output exception generated because %s",
                    io_exception.getMessage());

            throw new IPAException(caughtProblem);
        }
    }

    static native String hackPushIPAFile(IPAItemFront ipaItem);
    static native boolean hackPopIPAFile(int ipaFileDescriptorIndex);

}
