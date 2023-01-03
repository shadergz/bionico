#include <runtime_resources.h>

namespace hackback {
    std::shared_ptr<ipa::IpaManager> g_main_ipa_mgr;
    std::shared_ptr<SlayerLogger> g_logger;
    RawPointer<JNIEnv*> g_env;
    RawPointer<jobject> g_main_class;
}

