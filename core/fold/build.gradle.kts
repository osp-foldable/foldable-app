plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ohsang.play.fold.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(platform("androidx.compose:compose-bom:2025.06.00"))
    api("androidx.compose.ui:ui")
    // 접힘 감지
    api("androidx.window:window:1.4.0")
}
