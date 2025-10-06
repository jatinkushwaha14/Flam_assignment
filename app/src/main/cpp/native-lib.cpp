#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "OpenCVProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_opencvtest_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string hello = "OpenCV Edge Detection Ready!";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_opencvtest_MainActivity_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jintArray pixels,
        jint width,
        jint height) {

    LOGI("Processing frame: %dx%d", width, height);

    jint* pixelPtr = nullptr;  // Declare at function scope

    try {
        // Get pixel data from Java
        pixelPtr = env->GetIntArrayElements(pixels, nullptr);
        if (!pixelPtr) {
            LOGE("Failed to get pixel array");
            return nullptr;
        }

        // Create OpenCV Mat from pixel data (ARGB format)
        cv::Mat inputMat(height, width, CV_8UC4, (unsigned char*)pixelPtr);

        // Convert ARGB to RGB
        cv::Mat rgbMat;
        cv::cvtColor(inputMat, rgbMat, cv::COLOR_RGBA2RGB);

        // Convert to grayscale
        cv::Mat grayMat;
        cv::cvtColor(rgbMat, grayMat, cv::COLOR_RGB2GRAY);

        // Apply Canny edge detection
        cv::Mat edgesMat;
        cv::Canny(grayMat, edgesMat, 100, 200, 3);

        // Convert back to RGB (edges are white on black)
        cv::Mat outputRgb;
        cv::cvtColor(edgesMat, outputRgb, cv::COLOR_GRAY2RGB);

        // Convert back to ARGB for Android
        cv::Mat outputArgb;
        cv::cvtColor(outputRgb, outputArgb, cv::COLOR_RGB2RGBA);

        // Create output array
        jintArray result = env->NewIntArray(width * height);
        if (result) {
            env->SetIntArrayRegion(result, 0, width * height, (jint*)outputArgb.data);
        }

        // Release input array
        env->ReleaseIntArrayElements(pixels, pixelPtr, JNI_ABORT);

        LOGI("Frame processed successfully");
        return result;

    } catch (const std::exception& e) {
        LOGE("Processing error: %s", e.what());
        if (pixelPtr) {
            env->ReleaseIntArrayElements(pixels, pixelPtr, JNI_ABORT);
        }
        return nullptr;
    }
}
