#pragma once

#include <jni.h>
#include <string_view>
#include <vector>
#include <unistd.h>

static constexpr std::string_view g_BackEndLogTag = "HackinARM backend";

namespace HackinARM::Formats {
    class IPAArchive {
    public:
        jstring m_IPAPackageFilename{};
        jboolean m_FilenameCopyAdvisor;
        int m_UnixAccess{};

        jclass m_IPAItemClazz{};
        [[maybe_unused]] jclass m_ParentClass;
        JNIEnv* m_env;

        IPAArchive(JNIEnv* actualEnv, jclass parentClass, jobject IPAItemJNIObject) {
            m_env = actualEnv;
            m_ParentClass = parentClass;
            m_IPAItemClazz = actualEnv->GetObjectClass(IPAItemJNIObject);
            jfieldID fileDescriptorID = actualEnv->GetFieldID(m_IPAItemClazz,
                                                              "m_ParserFD",
                                                              "Landroid/os/ParcelFileDescriptor;");
            jobject fileDescriptorObject = actualEnv->GetObjectField(IPAItemJNIObject,
                                                                     fileDescriptorID);
            if (fileDescriptorObject == nullptr) {
                __android_log_print(ANDROID_LOG_DEBUG, g_BackEndLogTag.data(),
                                    "Can't found IPA file descriptor field ID");
                return;
            }

            jclass fdParserClass = actualEnv->GetObjectClass(fileDescriptorObject);
            jmethodID getFileMethod = actualEnv->GetMethodID(fdParserClass, "getFd",
                                                             "()I");
            m_UnixAccess = actualEnv->CallIntMethod(fileDescriptorObject,
                                                    getFileMethod);
            actualEnv->DeleteLocalRef(fdParserClass);

            if (m_UnixAccess <= 0) {
                __android_log_print(ANDROID_LOG_ERROR, g_BackEndLogTag.data(),
                                    "Can't fetch an real file descriptor for item %p",
                                    IPAItemJNIObject);
                return;
            }
            __android_log_print(ANDROID_LOG_DEBUG, g_BackEndLogTag.data(),
                                "File descriptor for %p: %d\n",
                                IPAItemJNIObject,
                                m_UnixAccess);

            fetchStorageFilename();
        }

        bool isFDValid() const {
            return m_UnixAccess > 0;
        }

        ~IPAArchive() {
            m_env->DeleteLocalRef(m_IPAItemClazz);
        }

        bool fetchStorageFilename() {
            if (m_IPAPackageFilename != nullptr) return false;
            char storageAccessPath[0x20];
            char stackFilenameBuffer[0x65];

            snprintf(storageAccessPath, sizeof(storageAccessPath),
                     "/proc/self/fd/%d", m_UnixAccess);
            const auto read_ret = readlink(storageAccessPath, stackFilenameBuffer,
                                      sizeof(stackFilenameBuffer));
            (read_ret)[stackFilenameBuffer] = '\0';

            __android_log_print(ANDROID_LOG_DEBUG, g_BackEndLogTag.data(),
                                "Real storage absolute filepath for %d fd: %s\n!",
                                m_UnixAccess, storageAccessPath);
            m_IPAPackageFilename = m_env->NewStringUTF(stackFilenameBuffer);

            return read_ret;
        }

        [[maybe_unused]] const std::string_view getStorageFilename() {
            auto rawString =  m_env->GetStringChars(m_IPAPackageFilename,
                                                    &m_FilenameCopyAdvisor);
            return reinterpret_cast<const char*>(rawString);
        }

    private:
    };

    class IPAManager {
        [[maybe_unused]] std::vector<std::shared_ptr<IPAArchive>> m_IPAList;

    public:
        IPAManager() = default;
        bool managerNewIPA(std::shared_ptr<IPAArchive> ipaArchive) {
            // Testing logical IPA UNIX file descriptor
            if (ipaArchive->isFDValid()) {
                m_IPAList.push_back(ipaArchive);
                __android_log_print(ANDROID_LOG_DEBUG, g_BackEndLogTag.data(),
                                    "New IPA item now begin controlled by our backend");
                return !m_IPAList.empty();
            }
            __android_log_print(ANDROID_LOG_ERROR, g_BackEndLogTag.data(),
                                "Unix file descriptor associated with the IPA item"
                                " isn't valid for now!");
            return false;
        }

        bool attemptRemoveIPA(int ipaDescriber) {
            bool cmp = false;
            std::remove_if(m_IPAList.begin(), m_IPAList.end(),
                           [cmp, ipaDescriber](auto ipaPackage) mutable {
                return cmp = ipaPackage->m_UnixAccess == ipaDescriber;
            });
            return cmp;
        }
    };
}

