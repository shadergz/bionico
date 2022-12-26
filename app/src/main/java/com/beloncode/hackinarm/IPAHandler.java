package com.beloncode.hackinarm;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

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

    private final Context mainActivityContext;

    IPAHandler(Context applicationContext) {
        mainActivityContext = applicationContext;
        mIPAItemList = new ArrayList<>();
    }

    private final ArrayList<IPAItem> mIPAItemList;

    // Translating an URI document into an absolute path name
    String pushIPAFromIPA(Uri IPAFilename) throws IPAException {
        assert IPAFilename != null: "IPAFilename can't be null";

        final IPAItem localIPAItem = new IPAItem();

        final ContentResolver mainResolver = mainActivityContext.getContentResolver();
        // File descriptor by himself
        final FileDescriptor fileDescriptor;

        try {
            localIPAItem.mParserFD = mainResolver.openFileDescriptor(IPAFilename, "r");

            ParcelFileDescriptor IPAParser = localIPAItem.mParserFD;
            fileDescriptor = IPAParser.getFileDescriptor();

            if (!fileDescriptor.valid()) {
                final String fdException = String.format("File Descriptor for %s not valid!",
                        IPAFilename);
                throw new IPAException(fdException);
            }
        } catch (IOException e) {
            final String ioErrorCause = String.format("IPAFilename can't be located, because %s",
                    e.getMessage());
            throw new IPAException(ioErrorCause);
        }

        localIPAItem.mFileDescriptor = fileDescriptor;

        final int lastReadableLength = mIPAItemList.size();
        final int currentReadableLength = openIPAStream(localIPAItem);
        assert lastReadableLength != currentReadableLength: "No one readable content added into "
            + "list";

        // Returning it's filename
        return localIPAItem.mIPAFilename;
    }

    private int openIPAStream(IPAItem IPAFile) throws IPAException {

        FileDescriptor fileDescriptor = IPAFile.mFileDescriptor;
        assert fileDescriptor.valid() : "File Descriptor must be valid for this operation";

        FileInputStream inputStream = new FileInputStream(fileDescriptor);
        byte[] localBuffer = new byte[10];
        try {
            // Testing whether we can properly read the file
            if (inputStream.read(localBuffer, 0, localBuffer.length) != -1) {
                throw new IPAException("The file is no longer available for our use!");
            }
            // Resenting internal file cursor
            inputStream.reset();
        } catch (IOException ioException) {
            final String ioExcept = String.format("IO Exception caused by %s",
                    ioException.getMessage());
            throw new IPAException(ioExcept);
        }
        // Now that the FileDescriptor is ready and it's valid, we can use it into Backend
        IPAFile.mFileInputStream = inputStream;

        IPAFile.mIPAFilename = hackPushIPAFile(IPAFile);
        if (IPAFile.mIPAFilename == null) {
            throw new IPAException("The IPA filename couldn't be fetched");
        }

        mIPAItemList.add(IPAFile);
        return mIPAItemList.size();
    }

    void deleteAllResources() throws IPAException {
        try {
            for (IPAItem readableContext : mIPAItemList) {
                // Feeding all resources used to handler an IPA Item
                final FileInputStream fileInputStream = readableContext.mFileInputStream;
                ParcelFileDescriptor contextParser = readableContext.mParserFD;

                contextParser.close();

                fileInputStream.close();
                mIPAItemList.remove(readableContext);
            }
        } catch (IOException ioException) {
            final String caughtProblem = String.format("IO Exception generated because %s",
                    ioException.getMessage());
            throw new IPAException(caughtProblem);
        }
    }

    static native String hackPushIPAFile(IPAItem fileDescriptor);

}
