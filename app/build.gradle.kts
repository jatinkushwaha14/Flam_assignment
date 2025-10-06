plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.opencvtest"  // Update to match your actual package
    compileSdk = 34

    defaultConfig {
        applicationId = "com.opencvtest"
        minSdk = 26  // Changed from 24 to 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++14")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // FIX: Align JVM targets to Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(":sdk"))  // This links OpenCV module
}
