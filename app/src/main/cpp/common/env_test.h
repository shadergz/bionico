#pragma once

#include <cassert>
#include <jni.h>

#include <common/references.h>

namespace hackback::common {
    void inline check_jni_params(RawPointer<JNIEnv*> jni_env, RawPointer<jobject> object) {
        assert(jni_env.is_valid() && object.is_valid());
    }
}