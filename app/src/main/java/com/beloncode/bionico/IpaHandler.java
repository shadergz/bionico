package com.beloncode.bionico;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

class IpaException extends Exception {
    public IpaException(String exceptMsg) {
        super(exceptMsg);
    }
}

public class IpaHandler {

    private final MainActivity mainActivity;
    private final ArrayList<IpaObject> ipaList;

    IpaHandler(MainActivity activity) {
        ipaList = new ArrayList<>();
        mainActivity = activity;
    }

    private boolean verifyForIpaOccurrence(final IpaObject ipaObject) {
        return ipaList.contains(ipaObject);
    }

    // Translating an URI document into an absolute path name
    public void handleNewIpa(@NonNull IpaObject ipaObject) throws IpaException {

        if (verifyForIpaOccurrence(ipaObject)) {
            // IPA is already inside our list, we can't added one more time!
            final String errorStr = String.format("Object with Uri %s already is inside our list!",
                    ipaObject.ipaUri.toString());

            mainActivity.getLogger().releaseMessage(FrontLogger.ERROR_LEVEL, errorStr, true);
            return;
        }
        final ContentResolver fMainResolver = mainActivity.getContentResolver();
        // File descriptor by himself
        final FileDescriptor fileDesc;

        try {
            // Opening the file for read and save his file descriptor inside the fd resolver!
            ipaObject.openableFParser = fMainResolver.openFileDescriptor(
                    ipaObject.getRegularUri(), "r");

            ParcelFileDescriptor ipaParser = ipaObject.openableFParser;
            fileDesc = ipaObject.fDescriptor = ipaParser.getFileDescriptor();

            if (!fileDesc.valid()) {
                final String fdException = String.format("File descriptor for %s isn't valid!",
                        ipaObject.ipaFilename);
                throw new IpaException(fdException);
            }

            if (!testIpaStreaming(ipaObject)) {
                mainActivity.getLogger().releaseMessage(FrontLogger.ERROR_LEVEL,
                        "Can't add the new ipa inside the list", false);
            }
        } catch (IOException ioExcept) {
            final String ioCause = String.format("Ipa filename can't be located, because %s",
                    ioExcept.getMessage());
            throw new IpaException(ioCause);
        }
    }

    private boolean fastIpaFileValidation(final byte[] ipaEntry) {
        final byte[] ipaHeader = {0x50, 0x4b};
        return ipaEntry[0] == ipaHeader[0] && ipaEntry[1] == ipaHeader[1];
    }

    private boolean checkFileExtensionWithMime(IpaObject ipaObject) throws IOException {
        final File ipaAsbFile = ipaObject.getRegularFile();
        if (!ipaAsbFile.getCanonicalPath().endsWith(".ipa")) return false;

        // Testing the file MIME type and finalizing the testing stage!
        final ContentResolver mimeResolver = mainActivity.getContentResolver();
        String extensionMime = mimeResolver.getType(ipaObject.ipaUri);

        return extensionMime.equals("application/octet-stream");
    }

    private boolean testIpaStreaming(IpaObject ipaObject) throws IpaException {

        FileDescriptor fdObject = ipaObject.fDescriptor;
        assert fdObject.valid() : "File descriptor must be valid for this operation";

        final FileInputStream iStream = ipaObject.readableStream = new FileInputStream(fdObject);
        final BufferedInputStream bufferReader = new BufferedInputStream(iStream);

        final byte[] localBuffer = new byte[10];
        try {
            // Testing whether we can properly read the file
            if ((bufferReader.read(localBuffer, 0, localBuffer.length)) == -1) {
                throw new IpaException("The file is no longer available for our use!");
            }

            if (!fastIpaFileValidation(localBuffer) && !checkFileExtensionWithMime(ipaObject)) {
                mainActivity.getLogger().releaseMessage(FrontLogger.ERROR_LEVEL,
                        "None a Ipa file, or may the file is corrupted!", true);
                return false;
            }

        } catch (IOException ioExcept) {
            final String exceptMessage = String.format("Input/Output Exception caused by %s",
                    ioExcept.getMessage());
            throw new IpaException(exceptMessage);
        }

        engineCtrlIpa(ipaObject);

        ipaList.add(ipaObject);
        return true;
    }

    void invalidateAllResources() throws IpaException {
        for (IpaObject ipaCollected : ipaList) {

            // Feeding all resources used to handler an IPA Item
            final FileInputStream fInput = ipaCollected.readableStream;
            ParcelFileDescriptor contextParser = ipaCollected.openableFParser;

            @SuppressLint("DefaultLocale") final String logRela = String.format("Destroying relationship with file descriptor %d",
                    contextParser.getFd());

            mainActivity.getLogger().releaseMessage(logRela);

            final int objectedDown = engineDownIpa(ipaCollected);
            if (objectedDown != -1) {
                @SuppressLint("DefaultLocale") final String objReport = String.format("Object with id %d removed", objectedDown);
                mainActivity.getLogger().releaseMessage(objReport);
            }

            try {
                fInput.close();

                if (contextParser.getFileDescriptor().valid())
                    contextParser.close();
            } catch (IOException ioException) {
                final String caughtProblem = String.format("Input/Output exception generated because %s",
                        ioException.getMessage());

                throw new IpaException(caughtProblem);
            }

            ipaList.remove(ipaCollected);
        }

    }

    static native int engineCtrlIpa(IpaObject ipaItem);

    static native int engineDownIpa(IpaObject ipaItem);

}
