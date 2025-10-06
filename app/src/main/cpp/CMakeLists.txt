cmake_minimum_required(VERSION 3.22.1)

project("opencvedgedetection")

# Set OpenCV directory to your actual path
set(OpenCV_DIR /Users/jatinkushwaha/opencv-android-sdk/sdk/native/jni)

# Include OpenCV headers
include_directories(/Users/jatinkushwaha/opencv-android-sdk/sdk/native/jni/include)

# Import OpenCV shared library
add_library(lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION /Users/jatinkushwaha/opencv-android-sdk/sdk/native/libs/arm64-v8a/libopencv_java4.so)

# Add your native library
add_library(
        opencvedgedetection
        SHARED
        native-lib.cpp
)

# Find required Android libraries
find_library(log-lib log)
find_library(android-lib android)

# Link libraries - include OpenCV
target_link_libraries(
        opencvedgedetection
        lib_opencv
        ${log-lib}
        ${android-lib}
)
