#pragma once

#include <cassert>
#include <jni.h>

#include <common/references.h>

namespace bionic::common {
    void inline checkJniParams(RawPointer<JNIEnv *> jni_env, RawPointer <jobject> object) {
        assert(jni_env.isValid() && object.isValid());
    }
}