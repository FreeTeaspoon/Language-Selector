import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val appVersionCode = 23
val appVersionName = "1.05.8"

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt)
    alias(libs.plugins.com.mikepenz.aboutlibraries)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
}

fun readMacKeychainPassword(service: String?, account: String?): String? {
    if (service.isNullOrBlank() || account.isNullOrBlank()) return null

    val security = File("/usr/bin/security")
    if (!security.exists()) return null

    return runCatching {
        val process = ProcessBuilder(
            security.absolutePath,
            "find-generic-password",
            "-s",
            service,
            "-a",
            account,
            "-w"
        ).redirectError(ProcessBuilder.Redirect.DISCARD).start()
        val password = process.inputStream.bufferedReader().use { it.readText().trim() }
        if (process.waitFor() == 0 && password.isNotEmpty()) password else null
    }.getOrNull()
}

val releaseStoreFile = providers.gradleProperty("releaseStoreFile").orNull
val releaseKeyAlias = providers.gradleProperty("releaseKeyAlias").orNull
val releaseKeychainService = providers.gradleProperty("releaseKeychainService").orNull
val releaseKeychainAccount = providers.gradleProperty("releaseKeychainAccount").orNull
val keychainPassword = readMacKeychainPassword(releaseKeychainService, releaseKeychainAccount)
val releaseStorePassword = providers.gradleProperty("releaseStorePassword").orNull ?: keychainPassword
val releaseKeyPassword = providers.gradleProperty("releaseKeyPassword").orNull ?: keychainPassword
val releaseSigningConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }
val releaseSigningPartiallyConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
    releaseKeychainService,
    releaseKeychainAccount
).any { !it.isNullOrBlank() } && !releaseSigningConfigured

if (releaseSigningPartiallyConfigured) {
    throw GradleException(
        "Release signing requires all four properties: " +
            "releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword"
    )
}

val releaseTaskRequested = gradle.startParameter.taskNames.any { taskName ->
    taskName.substringAfterLast(':').contains("release", ignoreCase = true)
}
if (releaseTaskRequested && !releaseSigningConfigured) {
    throw GradleException(
        "Release tasks require production signing. Configure the four release signing " +
            "properties or a macOS Keychain-backed release signing setup."
    )
}

android {
    namespace = "vegabobo.languageselector"
    compileSdk = 37

    defaultConfig {
        applicationId = "vegabobo.languageselector"
        minSdk = 33
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(requireNotNull(releaseStoreFile))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }
    buildTypes {
        release {
            signingConfig = if (releaseSigningConfigured) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
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
        checkReleaseBuilds = true
        abortOnError = true
    }

}

room {
    schemaDirectory("$projectDir/schemas")
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
    implementation(libs.miuix.nav)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.shader)
    implementation(libs.appiconloader)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigationevent)
    implementation(libs.androidx.navigationevent.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

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
