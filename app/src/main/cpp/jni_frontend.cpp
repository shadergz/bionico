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
    constexpr SOCId(std::string_view soc_name, SOCVendor vendor) : m_soc_name(soc_name),
        m_soc_vendor(vendor) {}
    [[maybe_unused]] const std::string_view m_soc_name;
    [[maybe_unused]] SOCVendor m_soc_vendor = SOCVendor::SOC_UNKNOWN;
};

[[maybe_unused]] constexpr std::tuple<SOCId, int> gs_compatibility[2] = {
        {{"SM7125", SOCVendor::SOC_SNAPDRAGON}, 10},
        {{"HelioG96", SOCVendor::SOC_MEDIATEK}, 01}
};

#define HACKBACK_EXPORT extern "C" JNIEXPORT

namespace hackback {
    using namespace common;

    HACKBACK_EXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackInitSystem
            (JNIEnv* env, jobject thiz) {
        g_env.assign(env);
        g_main_class.assign(thiz);
        check_jni_params(env, thiz);

        // Creating IPA Manager now, and locating its configuration
        g_logger = std::make_shared<SlayerLogger>();
        g_main_ipa_mgr = std::make_shared<ipa::IpaManager>();

        g_logger->back_echo("Backend system initialized!");

        return true;
    }
    HACKBACK_EXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackPause
            (JNIEnv *env, jobject thiz) {
        check_jni_params(env, thiz);
        return true;
    }
    HACKBACK_EXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackResume
            (JNIEnv *env, jobject thiz) {
        check_jni_params(env, thiz);
        return true;
    }
    HACKBACK_EXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackDestroy
            (JNIEnv *env, jobject thiz) {
        check_jni_params(env, thiz);
        return true;
    }

    HACKBACK_EXPORT jstring JNICALL
    Java_com_beloncode_hackinarm_IPAHandler_hackPushIPAFile(JNIEnv *env, jclass clazz,
                                                            jobject file_descriptor) {
        check_jni_params(env, clazz);

        auto ipa_backend_fmt = std::make_shared<ipa::detailed_format>(env, clazz,
                                                                      file_descriptor);
        g_main_ipa_mgr->manager_new_ipa(ipa_backend_fmt);
        return ipa_backend_fmt->m_ipa_filename.get_as_mut();
    }
    HACKBACK_EXPORT jboolean JNICALL
    Java_com_beloncode_hackinarm_IPAHandler_hackPopIPAFile(JNIEnv *env, jclass clazz,
                                                           jint ipa_file_descriptor_index) {
        check_jni_params(env, clazz);

        const auto ipa_result = g_main_ipa_mgr->attempt_rm_ipa(ipa_file_descriptor_index);
        if (ipa_result) {
            g_logger->back_echo("IPA package with {} was detached\n",
                                ipa_file_descriptor_index);
        }
        return ipa_result;
    }
}