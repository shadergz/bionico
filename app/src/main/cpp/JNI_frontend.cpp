#include <jni.h>
#include <android/log.h>
#include <string>

#include <runtime_resources.h>
#include <ENV_test.h>

extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackInitSystem
    (JNIEnv *env, jobject thiz) {
    HackinARM::Env::checkJavaEnv(env, thiz);
    __android_log_print(ANDROID_LOG_DEBUG, g_BackEndLogTag.data(), "Backend system "
                                                                   "initialized!");
    // Creating IPA Manager now, and locating its configuration
    HackinARM::Env::m_InitializedEnv = env;
    HackinARM::Env::m_AtCreateObject = thiz;
    HackinARM::Resources::g_MainIPAManager = std::make_shared<HackinARM::Formats::IPAManager>();

    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackPause
    (JNIEnv *env, jobject thiz) {
    HackinARM::Env::checkJavaEnv(env, thiz);
    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackResume
    (JNIEnv *env, jobject thiz) {
    HackinARM::Env::checkJavaEnv(env, thiz);
    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackDestroy
    (JNIEnv *env, jobject thiz) {
    HackinARM::Env::checkJavaEnv(env, thiz);
    return true;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_beloncode_hackinarm_IPAHandler_hackPushIPAFile(JNIEnv *env, jclass clazz,
                                                        jobject file_descriptor) {
    HackinARM::Env::checkJavaEnv(env, file_descriptor);
    auto IPA_backend_fmt = std::make_shared<HackinARM::Formats::IPAArchive>(
            env, clazz,file_descriptor);
    auto mainIPA = HackinARM::Resources::g_MainIPAManager;
    mainIPA->managerNewIPA(IPA_backend_fmt);
    return IPA_backend_fmt->m_IPAPackageFilename;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_beloncode_hackinarm_IPAHandler_hackPopIPAFile(JNIEnv *env, jclass clazz,
                                                       jint ipa_file_descriptor_index) {
    HackinARM::Env::checkJavaEnv(env, clazz);
    auto mainIPAHandler = HackinARM::Resources::g_MainIPAManager;
    const auto ipaResult = mainIPAHandler->attemptRemoveIPA(ipa_file_descriptor_index);
    if (ipaResult) {
        __android_log_print(ANDROID_LOG_DEBUG, g_BackEndLogTag.data(), "IPA package "
                                                                       "with %d has detached",
                                                                       ipa_file_descriptor_index);
    }
    return ipaResult;
}