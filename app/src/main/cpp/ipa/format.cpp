
#include <ipa/format.h>
#include <unistd.h>

namespace hackback::ipa {

    detailed_format::detailed_format(RawPointer<JNIEnv*> actual_env, RawPointer<jclass> parent_clazz,
            RawPointer<jobject> ipa_jni_object) {

        m_env.assign(actual_env);
        m_parent_class.assign(parent_clazz);
        m_ipa_clazz.assign(actual_env->GetObjectClass(ipa_jni_object.get()));

        RawPointer<jfieldID> file_descriptor_id = actual_env->GetFieldID(
                m_ipa_clazz.get(),"m_ParserFD","Landroid/os/ParcelFileDescriptor;");
        RawPointer<jobject> file_descriptor_object = actual_env->GetObjectField(
                ipa_jni_object.get(), file_descriptor_id.get());

        if (!file_descriptor_object.is_valid()) {
            g_logger->back_echo("Can't found IPA file descriptor field ID");
            return;
        }

        RawPointer<jclass> fd_parser_class = actual_env.get()->GetObjectClass(
                file_descriptor_object.get());
        RawPointer<jmethodID> get_file_method = actual_env->GetMethodID(
                fd_parser_class.get(), "getFd","()I");

        m_fd_access = actual_env->CallIntMethod(
                file_descriptor_object.get(),get_file_method.get());

        actual_env->DeleteLocalRef(fd_parser_class.get());
        if (m_fd_access <= 0) {
            g_logger->back_echo("Can't fetch an real file descriptor for item: {}",
                                static_cast<void*>(ipa_jni_object.get()));
            return;
        }

        g_logger->back_echo("File descriptor for {}: {}\n",
                            static_cast<void*>(ipa_jni_object.get()),
                            m_fd_access);

        fetch_storage_filename();
    }

    bool detailed_format::fetch_storage_filename() {

        if (m_ipa_filename != nullptr) return false;

        char storage_access_path[0x20];
        char filename_buffer[0x65];

        snprintf(storage_access_path, sizeof(storage_access_path),
                 "/proc/self/fd/%d", m_fd_access);
        const auto read_ret = readlink(storage_access_path, filename_buffer,
                                       sizeof(filename_buffer));
        (read_ret)[filename_buffer] = '\0';

        m_ipa_filename = m_env->NewStringUTF(filename_buffer);

        g_logger->back_echo("Real storage absolute filepath for {} fd: {}\n!", m_fd_access,
                            filename_buffer);
        return read_ret != 0;
    }

}
