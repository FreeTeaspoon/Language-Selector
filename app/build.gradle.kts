import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt)
    alias(libs.plugins.com.mikepenz.aboutlibraries)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "vegabobo.languageselector"
    compileSdk = 37

    defaultConfig {
        applicationId = "vegabobo.languageselector"
        minSdk = 33
        targetSdk = 37
        versionCode = 9
        versionName = "1.04.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        compose = true
        aidl = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

aboutLibraries {
    excludeFields = arrayOf("generated")
}

hilt {
    enableAggregatingTask = false
}

dependencies {
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation(libs.libsu.core)
    implementation(libs.libsu.service)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.navigation3.ui)
    implementation(libs.appiconloader)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigationevent)
    implementation(libs.androidx.navigationevent.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.aboutlibraries.core)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    implementation(libs.hiddenapibypass)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    compileOnly(project(":hidden_api"))

    testImplementation(libs.junit)
}
