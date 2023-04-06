#include <runtime_resources.h>

namespace bionic {
    std::shared_ptr<ipa::IpaManager> gMainIpaMgr;
    std::shared_ptr<SlayerLogger> gLogger;
    RawPointer<JNIEnv *> gEnv;
    RawPointer<jobject> gMainClass;
}

