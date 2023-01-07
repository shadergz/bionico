#pragma once

#include <jni.h>

#include <memory>
#include <common/references.h>

namespace hackback::ipa {
    class IpaManager;

    class IpaManager;
}

namespace hackback {
    class SlayerLogger;

    using namespace common;

    extern std::shared_ptr<ipa::IpaManager> g_main_ipa_mgr;
    extern std::shared_ptr<SlayerLogger> g_logger;
    extern RawPointer<JNIEnv *> g_env;
    extern RawPointer<jobject> g_main_class;

}
