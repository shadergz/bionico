#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackInitSystem
    (JNIEnv *env, jobject thiz) {
    (void)env;
    (void)thiz;
    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackPause
    (JNIEnv *env, jobject thiz) {
    (void)env;
    (void)thiz;
    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackResume
    (JNIEnv *env, jobject thiz) {
    (void)env;
    (void)thiz;
    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL Java_com_beloncode_hackinarm_MainActivity_hackDestroy
    (JNIEnv *env, jobject thiz) {
    (void)env;
    (void)thiz;
    return true;
}