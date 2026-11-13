plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Google 공식 테스트 ID
val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
val TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"

android {
    namespace = "com.ohsang.util.foldpick"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ohsang.util.foldpick"
        minSdk = 29
        targetSdk = 36
        // CI에선 빌드 번호로 자동 증가, 로컬에선 1
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        // 태그 number-v1.2.0 → 1.2.0
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
    }

    // 출시 서명: CI가 Secrets로 만든 keystore 경로·비밀번호를 환경 변수로 넘겨줌
    val keystorePath = System.getenv("KEYSTORE_PATH")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        // 디버그는 항상 Google 테스트 광고 (실제 광고를 직접 누르면 계정 정지 위험)
        debug {
            manifestPlaceholders["admobAppId"] = TEST_ADMOB_APP_ID
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"$TEST_BANNER_ID\"")
        }
        // 출시는 CI가 Secrets(PICK_ADMOB_APP_ID, PICK_ADMOB_BANNER_ID)로 넘겨준 실제 ID.
        // 없으면 테스트 ID로 빌드되고 경고만 남김
        release {
            val appId = System.getenv("ADMOB_APP_ID")
            val bannerId = System.getenv("ADMOB_BANNER_ID")
            if (appId.isNullOrBlank() || bannerId.isNullOrBlank()) {
                logger.warn("⚠️ ADMOB_APP_ID / ADMOB_BANNER_ID 없음 → 테스트 광고 ID로 빌드")
            }
            manifestPlaceholders["admobAppId"] = appId?.takeIf { it.isNotBlank() } ?: TEST_ADMOB_APP_ID
            buildConfigField(
                "String", "BANNER_AD_UNIT_ID",
                "\"${bannerId?.takeIf { it.isNotBlank() } ?: TEST_BANNER_ID}\"",
            )
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
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
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:fold"))

    val composeBom = platform("androidx.compose:compose-bom:2025.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
}
