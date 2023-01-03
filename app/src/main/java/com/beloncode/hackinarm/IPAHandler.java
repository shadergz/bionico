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

class IPAException extends Exception {
    public IPAException(String exceptionMessage) {
        super(exceptionMessage);
    }
}

public class IPAHandler {

    private final Context p_mainActivityContext;
    private final HackLogger p_activateLogger;

    IPAHandler(Context applicationContext, HackLogger logger) {
        p_activateLogger = logger;
        p_mainActivityContext = applicationContext;
        m_IPAItemList = new ArrayList<>();
    }

    private final ArrayList<IPAItemFront> m_IPAItemList;

    // Translating an URI document into an absolute path name
    final String pushIPAFromIPA(@NonNull Uri ipaFilename) throws IPAException {

        final IPAItemFront localIPAItem = new IPAItemFront();

        final ContentResolver mainResolver = p_mainActivityContext.getContentResolver();
        // File descriptor by himself
        final FileDescriptor fileDescriptor;

        try {
            // Opening the file for read and save his file descriptor inside the fd resolver!
            localIPAItem.m_ParserFD = mainResolver.openFileDescriptor(ipaFilename, "r");

            ParcelFileDescriptor IPAParser = localIPAItem.m_ParserFD;
            fileDescriptor = IPAParser.getFileDescriptor();

            if (!fileDescriptor.valid()) {
                final String fdException = String.format("File descriptor for %s not valid!",
                        ipaFilename);
                throw new IPAException(fdException);
            }
        } catch (IOException e) {
            final String ioErrorCause = String.format("IPA filename can't be located, because %s",
                    e.getMessage());
            throw new IPAException(ioErrorCause);
        }

        localIPAItem.m_FileDescriptor = fileDescriptor;

        final int lastReadableLength = m_IPAItemList.size();
        final int currentReadableLength = openIPAStream(localIPAItem);
        assert lastReadableLength != currentReadableLength: "No one readable content added into "
            + "list";

        // Returning it's filename
        return localIPAItem.m_IPAFilename;
    }

    private int openIPAStream(IPAItemFront ipaFile) throws IPAException {

        FileDescriptor fileDescriptor = ipaFile.m_FileDescriptor;
        assert fileDescriptor.valid() : "File descriptor must be valid for this operation";

        final FileInputStream inputStream = new FileInputStream(fileDescriptor);
        final BufferedInputStream bufferReader = new BufferedInputStream(inputStream);

        final byte[] localBuffer = new byte[10];
        try {
            // Testing whether we can properly read the file
            if ((bufferReader.read(localBuffer, 0, localBuffer.length)) == -1) {
                throw new IPAException("The file is no longer available for our use!");
            }
            // Resenting internal file cursor
            //bufferReader.reset();
        } catch (IOException ioException) {
            final String ioExcept = String.format("Input/Output Exception caused by %s",
                    ioException.getMessage());
            throw new IPAException(ioExcept);
        }
        // Now that the FileDescriptor is ready and it's valid, we can use it into Backend
        ipaFile.m_FileInputStream = inputStream;

        ipaFile.m_IPAFilename = hackPushIPAFile(ipaFile);
        if (ipaFile.m_IPAFilename == null) {
            throw new IPAException("The IPA package filename couldn't be fetched");
        }

        m_IPAItemList.add(ipaFile);
        return m_IPAItemList.size();
    }

    void deleteAllResources() throws IPAException {
        try {
            for (IPAItemFront ipaItem : m_IPAItemList) {
                // Feeding all resources used to handler an IPA Item
                final FileInputStream fileInputStream = ipaItem.m_FileInputStream;
                ParcelFileDescriptor contextParser = ipaItem.m_ParserFD;
                @SuppressLint("DefaultLocale") final String logRela = String.format(
                        "Destroying relationship with file descriptor %d\n", contextParser.getFd());
                p_activateLogger.release(logRela);
                final int ipaFile = contextParser.getFd();
                assert hackPopIPAFile(ipaFile);

                fileInputStream.close();
                if (contextParser.getFileDescriptor().valid())
                    contextParser.close();
                m_IPAItemList.remove(ipaItem);
            }
        } catch (IOException ioException) {
            final String caughtProblem = String.format("Input/Output exception generated because %s",
                    ioException.getMessage());

            throw new IPAException(caughtProblem);
        }
    }

    static native String hackPushIPAFile(IPAItemFront ipaItem);
    static native boolean hackPopIPAFile(int ipaFileDescriptorIndex);

}
