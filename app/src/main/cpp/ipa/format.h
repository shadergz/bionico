#pragma once

#include <jni.h>

#include <string_view>

#include <common/references.h>
#include <slayer_logger.h>
#include <runtime_resources.h>

namespace bionic::ipa {
    using namespace common;

    class DetailedFormat {
    public:
        DetailedFormat(RawPointer<JNIEnv *> actual_env, RawPointer<jclass> parent_clazz,
                       RawPointer<jobject> ipa_jni_object);

        ~DetailedFormat() {
            mEnv->DeleteLocalRef(mIpaClazz.get());
        }

        bool isFdValid() const {
            return mFdAccess > 0;
        }

        bool fetchStorageFilename();

        std::unique_ptr<std::string> mIpaFilename;
        int mFdAccess{};

        RawPointer<jclass> mIpaClazz{};
        RawPointer<jclass> mParentClass{};
        RawPointer<JNIEnv *> mEnv{};
    };

    using ipa_object = std::shared_ptr<DetailedFormat>;
}

