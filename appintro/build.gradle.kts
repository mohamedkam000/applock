plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.muhammad.appintro"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    implementation("androidx.core:core-ktx:1.13.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0-alpha01")
    implementation("androidx.activity:activity-compose:1.9.0-alpha01")
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.compose.ui:ui:1.6.0-alpha03")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0-alpha03")
    implementation("androidx.compose.material3:material3:1.2.0-alpha05")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
}