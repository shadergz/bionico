#pragma once

#include <jni.h>

#include <string_view>

#include <common/references.h>
#include <slayer_logger.h>
#include <runtime_resources.h>

namespace hackback::ipa {
    using namespace common;

    class detailed_format {
    public:
        detailed_format(RawPointer<JNIEnv*> actual_env, RawPointer<jclass> parent_clazz,
                        RawPointer<jobject> ipa_jni_object);

        ~detailed_format() {
            m_env->DeleteLocalRef(m_ipa_clazz.get());
        }

        bool is_fd_valid() const {
            return m_fd_access > 0;
        }

        bool fetch_storage_filename();

        [[maybe_unused]] const std::string_view get_storage_filename() {
            auto raw_string = m_env->GetStringChars(m_ipa_filename.get(),
                                                    &m_fn_is_copied);
            return reinterpret_cast<const char *>(raw_string);
        }

        RawPointer<jstring> m_ipa_filename{};
        jboolean m_fn_is_copied{};
        int m_fd_access{};

        RawPointer<jclass> m_ipa_clazz{};
        RawPointer<jclass> m_parent_class{};
        RawPointer<JNIEnv*> m_env{};
    };

    using ipa_object = std::shared_ptr<detailed_format>;
}

