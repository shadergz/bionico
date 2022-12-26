#pragma once

#include <jni.h>

#include <vector>

static constexpr std::string_view gBackEndLogTag = "HackinARM backend";

namespace HackinARM::Formats {
    class IPAArchive {
        std::string mIPAFilename;
        int mUNIXDirectOpen{};

        [[maybe_unused]] jclass IPAItemJava{};
        [[maybe_unused]] jclass mParentClass;

    public:
        IPAArchive(JNIEnv* actualEnv, jclass parentClass, jobject IPAItemJNIObject) {
            mParentClass = parentClass;
            IPAItemJava = actualEnv->GetObjectClass(IPAItemJNIObject);
            jfieldID fileDescriptorID = actualEnv->GetFieldID(IPAItemJava, "mFileDescriptor", "IPAItem");
            jobject fileDescriptorObject = actualEnv->GetObjectField(IPAItemJNIObject, fileDescriptorID);

            if (fileDescriptorObject == nullptr) {
                __android_log_print(ANDROID_LOG_DEBUG, gBackEndLogTag.data(),
                                    "Can't found IPA File Descriptor Field ID");
                return;
            }

            if (!loadRealFD(actualEnv, fileDescriptorObject)) {
                __android_log_print(ANDROID_LOG_ERROR, gBackEndLogTag.data(),
                                    "Can't fetch an real file descriptor for item %p", IPAItemJNIObject);
                return;
            }
        }

        bool isUNIXFDValid() const {
            return mUNIXDirectOpen > 0;
        }

        [[maybe_unused]] const std::string_view fetchFilename() const {
            return mIPAFilename;
        }

    private:
        bool loadRealFD(JNIEnv* actualEnv, jobject fileDescriptor) {
            if (fileDescriptor == nullptr)
                return false;
            (void)actualEnv;
            return isUNIXFDValid();
        }

    };

    class IPAManager {
        [[maybe_unused]] std::vector<std::weak_ptr<IPAArchive>> mIPAList;

    public:
        IPAManager() = default;
        bool managerNewIPA(std::shared_ptr<IPAArchive> ipaArchive) {
            // Testing logical IPA UNIX file descriptor
            if (ipaArchive->isUNIXFDValid()) {
                mIPAList.push_back(ipaArchive);
                __android_log_print(ANDROID_LOG_DEBUG, gBackEndLogTag.data(),
                                    "New IPA item now begin controlled by our backend");
                return !mIPAList.empty();
            }
            __android_log_print(ANDROID_LOG_ERROR, gBackEndLogTag.data(),
                                "Unix file descriptor associated with the IPA item"
                                " isn't valid for now!");
            return false;
        }
    };
}

