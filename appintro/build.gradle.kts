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
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0-alpha02")
    implementation("androidx.activity:activity-compose:1.12.0-alpha06")
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.compose.ui:ui:1.10.0-alpha01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.0-alpha01")
    implementation("androidx.compose.material3:material3:1.5.0-alpha02")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
}