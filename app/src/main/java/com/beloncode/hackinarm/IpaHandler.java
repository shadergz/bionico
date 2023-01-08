package com.beloncode.hackinarm;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class IpaHandler extends MainActivity {
    private final ArrayList<IpaObject> ipaList;

    IpaHandler() {
        ipaList = new ArrayList<>();
    }

    ActivityResultLauncher<Intent> getIpaFromContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null || result.getResultCode() != RESULT_OK) {
                    final String errorOne = "Can't open the file or no file has been selected";
                    mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, errorOne, true);
                    return;
                }
                Intent ipaIntent = result.getData();
                IpaObject newIpaObject = new IpaObject(ipaIntent);

                final File ipaFile = newIpaObject.getRegularFile();

                if (!ipaFile.isFile() || !ipaFile.canRead()) {
                    final String cantRead = String.format("Can't read file (%s), isn't a regular file!",
                            ipaFile.getAbsolutePath());
                    mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, cantRead);
                }

                try {
                    handleNewIpa(newIpaObject);
                    mainCoreInstaller.installNewIpa(newIpaObject);

                } catch (IpaException ipaException) {
                    final String ipaError = String.format("Error occurred while tried to handler a " +
                            "new Ipa object, with file path: %s", ipaFile.getAbsolutePath());
                    mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, ipaError);
                }

                mainIpaAdapter.placeNewItem(newIpaObject);
                final String toastMessage = String.format("Adding an Ipa package with pathname: %s",
                        newIpaObject.ipaFilename);
                mainLogger.releaseMessage(HackLogger.USER_LEVEL, toastMessage, true);
            });

    public void selectIpaFile() {
        Intent openDocumentProvider = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        openDocumentProvider.setType("*/*");
        openDocumentProvider.addCategory(Intent.CATEGORY_OPENABLE);

        getIpaFromContent.launch(openDocumentProvider);
    }

    private boolean verifyForIpaOccurrence(final IpaObject ipaObject) {
        return ipaList.contains(ipaObject);
    }

    // Translating an URI document into an absolute path name
    public void handleNewIpa(@NonNull IpaObject ipaObject) throws IpaException {

        if (verifyForIpaOccurrence(ipaObject)) {
            // IPA is already inside our list, we can't added one more time!
            final String errorStr = String.format("Object with Uri %s already inside our list!",
                    ipaObject.ipaUri.toString());

            mainLogger.releaseMessage(HackLogger.ERROR_LEVEL, errorStr, true);
            return;
        }
        final ContentResolver fMainResolver = getContentResolver();
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
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL,
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

            if (!fastIpaFileValidation(localBuffer)) {
                mainLogger.releaseMessage(HackLogger.ERROR_LEVEL,
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

            mainLogger.releaseMessage(logRela);

            final int objectedDown = engineDownIpa(ipaCollected);
            if (objectedDown != -1) {
                @SuppressLint("DefaultLocale")
                final String objReport = String.format("Object with id %d removed",
                        objectedDown);
                mainLogger.releaseMessage(objReport);
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
        getIpaFromContent.unregister();
    }

    static native int engineCtrlIpa(IpaObject ipaItem);

    static native int engineDownIpa(IpaObject ipaItem);

}
