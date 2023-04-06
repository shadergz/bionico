#pragma once

#include <jni.h>

#include <memory>
#include <common/references.h>

namespace bionic::ipa {
    class IpaManager;

    class IpaManager;
}

namespace bionic {
    class SlayerLogger;

    using namespace common;

    extern std::shared_ptr<ipa::IpaManager> gMainIpaMgr;
    extern std::shared_ptr<SlayerLogger> gLogger;
    extern RawPointer<JNIEnv *> gEnv;
    extern RawPointer<jobject> gMainClass;

}
