#include <memory>

#include <ipa/format.h>
#include <unistd.h>

namespace bionic::ipa {

    DetailedFormat::DetailedFormat(RawPointer<JNIEnv *> actual_env,
                                   RawPointer<jclass> parent_clazz,
                                   RawPointer<jobject> ipa_jni_object) {

        mEnv.assign(actual_env);
        mParentClass.assign(parent_clazz);
        mIpaClazz.assign(actual_env->GetObjectClass(ipa_jni_object.get()));

        RawPointer<jfieldID> file_descriptor_id = actual_env->GetFieldID(
                mIpaClazz.get(), "fDescriptor", "Landroid/os/ParcelFileDescriptor;");
        RawPointer<jobject> file_descriptor_object = actual_env->GetObjectField(
                ipa_jni_object.get(), file_descriptor_id.get());

        if (!file_descriptor_object.isValid()) {
            gLogger->backEcho("Can't found Ipa file descriptor 'fDescriptor' field\n");
            return;
        }

        RawPointer<jclass> fd_parser_class = actual_env->GetObjectClass(
                file_descriptor_object.get());
        RawPointer<jmethodID> get_file_method = actual_env->GetMethodID(
                fd_parser_class.get(), "getFd", "()I");

        mFdAccess = actual_env->CallIntMethod(
                file_descriptor_object.get(), get_file_method.get());

        actual_env->DeleteLocalRef(fd_parser_class.get());
        if (mFdAccess <= 0) {
            gLogger->backEcho("Can't fetch an real file descriptor for item: {}\n",
                              static_cast<void *>(ipa_jni_object.get()));
            return;
        }

        gLogger->backEcho("fDescriptor for {}: {}\n",
                          static_cast<void *>(ipa_jni_object.get()), mFdAccess);

        fetchStorageFilename();
    }

    bool DetailedFormat::fetchStorageFilename() {

        if (!mIpaFilename->empty()) return false;

        constexpr uint STORAGE_ACCESS_SZ = 0x20;
        constexpr uint FILENAME_BUFFER_SZ = 0x65;

        char storage_access_path[STORAGE_ACCESS_SZ];
        char filename_buffer[FILENAME_BUFFER_SZ];

        snprintf(storage_access_path, sizeof(storage_access_path),
                 "/proc/self/fd/%d", mFdAccess);
        const auto read_ret = readlink(storage_access_path, filename_buffer,
                                       sizeof(filename_buffer));
        (read_ret)[filename_buffer] = '\0';

        mIpaFilename = std::make_unique<std::string>(filename_buffer);

        gLogger->backEcho("Real storage absolute filepath for {} fd: {}\n", mFdAccess,
                          filename_buffer);
        return true;
    }

}
