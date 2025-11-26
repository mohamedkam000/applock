plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.app.lock"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.lock"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1"
        ndk {
            abiFilters += listOf("arm64-v8a")
//            debugSymbolLevel  'none'
        }

        androidResources {
            localeFilters += setOf("en")
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
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.activity:activity-ktx:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation(platform("androidx.compose:compose-bom:2025.11.01"))
    implementation("androidx.compose.ui:ui:1.10.0-rc01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.0-rc01")
    implementation("androidx.compose.material3:material3:1.5.0-alpha09")
    implementation("androidx.biometric:biometric:1.4.0-alpha04")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.9.6")
}