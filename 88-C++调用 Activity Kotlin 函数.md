###  C++ 中调用 Android Kotlin 函数

```cpp
#include <jni.h>
#include <string>
#include <cassert>


    // 调用普通函数，传入 string
    extern "C"
    JNIEXPORT void JNICALL
    Java_com_sjguj_MainActivity_onRresume(JNIEnv *env, jobject thiz) {
        std::string hello = "https://www.google.com";
        jclass mainActivityClass = (*env).FindClass("com/sjguj/MainActivity");
        assert(mainActivityClass != nullptr);
        jmethodID mid = env->GetMethodID(mainActivityClass, "startWeb", "(Ljava/lang/String;)V");
        assert(mid != nullptr);
        env->CallVoidMethod(thiz, mid, env->NewStringUTF(hello.c_str()));
    }


    // 分享，调用普通函数
    extern "C"
    JNIEXPORT void JNICALL
    Java_com_sjguj_MainActivity_onShare(JNIEnv *env, jobject thiz) {
        std::string hello = "uiuiuiuiuiuiuiuuii";
        jclass mainActivityClass = (*env).FindClass("com/sjguj/MainActivity");
        assert(mainActivityClass != nullptr);
        jmethodID mid = env->GetMethodID(mainActivityClass, "share", "(Ljava/lang/String;)V");
        assert(mid != nullptr);
        env->CallVoidMethod(thiz, mid, env->NewStringUTF(hello.c_str()));
    }


    // 分享，调用 kotlin 静态函数
    extern "C"
    JNIEXPORT void JNICALL
    Java_com_sjguj_MainActivity_onShare2(JNIEnv *env, jobject thiz) {
        std::string hello = "uiuiuiuiuiuiuiuuii";

        jclass mainActivityClass = (*env).FindClass("com/sjguj/MainActivity");
        assert(mainActivityClass != nullptr);

        jmethodID mid = env->GetStaticMethodID(mainActivityClass, "share2", "(Ljava/lang/String;)V");
        assert(mid != nullptr);

        env->CallStaticVoidMethod(mainActivityClass, mid, env->NewStringUTF(hello.c_str()));
    }
```