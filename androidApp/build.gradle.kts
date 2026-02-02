plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.crazymobileapp.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.crazymobileapp.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":shared"))
}
