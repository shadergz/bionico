#include <jni.h>
#include <android/log.h>
#include <string>

#include "runtime_resources.h"
#include "ENV_test.h"

extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackInitSystem
    (JNIEnv *env, jobject thiz) {
    HackinARM::Env::checkJavaEnv(env, thiz);
    __android_log_print(ANDROID_LOG_DEBUG, gBackEndLogTag.data(), "Backend system initialized!");
    // Creating IPA Manager now, and locating its configuration
    HackinARM::Env::mInitializedEnv = env;
    HackinARM::Env::mAtCreateObject = thiz;
    HackinARM::Resources::gMainIPAManager = std::make_shared<HackinARM::Formats::IPAManager>();

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
    std::shared_ptr<HackinARM::Formats::IPAArchive> IPABackendFormat;
    IPABackendFormat = std::make_shared<HackinARM::Formats::IPAArchive>(env, clazz, file_descriptor);
    auto MainIPAHandler = HackinARM::Resources::gMainIPAManager;
    MainIPAHandler->managerNewIPA(IPABackendFormat);
    return nullptr;
}