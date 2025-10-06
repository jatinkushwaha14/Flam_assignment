#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_yourpackage_opencvedgedetection_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    LOGI("Native method called successfully");

    std::string hello = "Hello from C++ with OpenCV!";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_yourpackage_opencvedgedetection_MainActivity_testOpenCV(
        JNIEnv* env,
        jobject /* this */) {

    LOGI("Testing OpenCV integration");

    try {
        // Create a test OpenCV Mat
        cv::Mat test_image = cv::Mat::zeros(480, 640, CV_8UC3);

        // Draw a simple rectangle
        cv::rectangle(test_image, cv::Point(100, 100), cv::Point(300, 200), cv::Scalar(0, 255, 0), 2);

        // Apply Canny edge detection
        cv::Mat gray, edges;
        cv::cvtColor(test_image, gray, cv::COLOR_BGR2GRAY);
        cv::Canny(gray, edges, 100, 200);

        std::string result = "OpenCV working! Created " +
                             std::to_string(test_image.rows) + "x" +
                             std::to_string(test_image.cols) + " image with Canny edges";

        LOGI("OpenCV test successful: %s", result.c_str());

        return env->NewStringUTF(result.c_str());

    } catch (const std::exception& e) {
        std::string error = "OpenCV Error: " + std::string(e.what());
        LOGI("OpenCV test failed: %s", error.c_str());
        return env->NewStringUTF(error.c_str());
    }
}
