plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "dev.muhammad.applock"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.muhammad.applock"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("sign.p12")
            storePassword = "8075"
            keyAlias = "sign"
            keyPassword = "8075"
            storeType = "pkcs12"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":appintro"))
    
    
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.compose.ui:ui:1.10.0-alpha01")
    implementation("androidx.compose.animation:animation:1.9.0-rc01")
    implementation("androidx.compose.ui:ui-tooling:1.10.0-alpha01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.0-alpha01")
    implementation("androidx.activity:activity-compose:1.12.0-alpha06")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.runtime:runtime:1.10.0-alpha01")
    implementation("androidx.compose.material3:material3:1.5.0-alpha02")
    implementation("androidx.navigation:navigation-compose:2.9.3")
//    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0-alpha04")
    implementation("androidx.activity:activity-ktx:1.12.0-alpha06")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0-alpha02")
//    implementation("com.airbnb.android:lottie-compose:6.6.7")
//    implementation("com.airbnb.android:lottie:6.6.7")
//    implementation("com.github.spotbugs:spotbugs-annotations:4.9.3")
//    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
//    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")



//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(libs.androidx.activity.ktx)
//    implementation(libs.androidx.fragment.ktx)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
    implementation(libs.androidx.biometric)
//    implementation(libs.androidx.material.icons.core)
//    implementation(libs.androidx.material.icons.extended)
//    implementation(libs.androidx.navigation.compose)
//    implementation(libs.shizuku.api)
//    implementation(libs.shizuku.provider)
//    implementation("dev.rikka.tools.refine:runtime:4.4.0")
//    compileOnly(project(":hidden-api"))
//    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
}