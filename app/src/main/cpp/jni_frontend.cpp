#include <string>

#include <ipa/manager.h>

#include <common/env_test.h>
#include <slayer_logger.h>
#include <runtime_resources.h>

enum class SOCVendor {
    SOC_UNKNOWN,
    SOC_SNAPDRAGON,
    SOC_MEDIATEK
};

class SOCId {
public:
    constexpr SOCId(std::string_view soc_name, SOCVendor vendor) : mSocName(soc_name),
                                                                   mSocVendor(vendor) {}

    [[maybe_unused]] const std::string_view mSocName;
    [[maybe_unused]] SOCVendor mSocVendor = SOCVendor::SOC_UNKNOWN;
};

[[maybe_unused]] constexpr std::tuple<SOCId, int> gs_compatibility[2] = {
        {{"SM7125",   SOCVendor::SOC_SNAPDRAGON}, 10},
        {{"HelioG96", SOCVendor::SOC_MEDIATEK},   01}
};

#define BIOCORE_EXPORT extern "C" JNIEXPORT

namespace bionic {
    using namespace common;

    BIOCORE_EXPORT jboolean JNICALL Java_com_beloncode_bionico_MainActivity_engineInitSystem
            (JNIEnv *env, jobject thiz) {
        gEnv.assign(env);
        gMainClass.assign(thiz);
        checkJniParams(env, thiz);

        // Creating Ipa Manager now, and locating it's configuration
        gLogger = std::make_shared<SlayerLogger>();
        gMainIpaMgr = std::make_shared<ipa::IpaManager>();

        gLogger->backEcho("Backend system initialized!\n");

        return true;
    }
    BIOCORE_EXPORT jboolean JNICALL Java_com_beloncode_bionico_MainActivity_enginePause
            (JNIEnv *env, jobject thiz) {
        checkJniParams(env, thiz);
        return true;
    }
    BIOCORE_EXPORT jboolean JNICALL Java_com_beloncode_bionico_MainActivity_engineResume
            (JNIEnv *env, jobject thiz) {
        checkJniParams(env, thiz);
        return true;
    }
    BIOCORE_EXPORT jboolean JNICALL Java_com_beloncode_bionico_MainActivity_engineDestroy
            (JNIEnv *env, jobject thiz) {
        checkJniParams(env, thiz);
        return true;
    }

    BIOCORE_EXPORT jint JNICALL
    Java_com_beloncode_bionico_IpaHandler_engineCtrlIpa(JNIEnv *env, jclass clazz, jobject file_descriptor) {
        checkJniParams(env, clazz);

        auto ipa_object = std::make_shared<ipa::DetailedFormat>(env, clazz,
                                                                file_descriptor);
        gMainIpaMgr->managerNewIpa(ipa_object);
        return gMainIpaMgr->findIpaIndex(ipa_object);
    }
    BIOCORE_EXPORT jint JNICALL
    Java_com_beloncode_bionico_IpaHandler_engineDownIpa(JNIEnv *env, jclass clazz,
                                                          jobject ipa_item) {
        checkJniParams(env, clazz);

        auto ipa_block = std::make_shared<ipa::DetailedFormat>(env, clazz, ipa_item);

        const jint ipa_location = gMainIpaMgr->findIpaIndex(ipa_block);
        const bool ipa_result = gMainIpaMgr->attemptRmIpa(ipa_block);

        if (ipa_result) {
            gLogger->backEcho("Ipa package located at {} was detached\n",
                              static_cast<void *>(ipa_item));
        }

        return ipa_location;
    }
}
