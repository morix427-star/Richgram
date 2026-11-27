plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.richgram.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.richgram.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {\n        compose = true\n        buildConfig = true\n    }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")\n    implementation("io.github.tdlib-android:core:0.1.1")\n    implementation("io.github.tdlib-android:ktx:0.1.1")\n    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")\n    val telegramApiId = System.getenv("TELEGRAM_API_ID") ?: ""\n    val telegramApiHash = System.getenv("TELEGRAM_API_HASH") ?: ""\n    defaultConfig {\n        buildConfigField("String", "TELEGRAM_API_ID", "\\\"$telegramApiId\\\"")\n        buildConfigField("String", "TELEGRAM_API_HASH", "\\\"$telegramApiHash\\\"")\n    }
}
