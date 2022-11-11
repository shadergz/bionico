#include <jni.h>
#include <android/log.h>
#include <string>

#include "Runtime_Resources.h"
#include "ENV_Test.h"

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