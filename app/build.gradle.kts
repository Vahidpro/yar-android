import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val signingVariables = listOf(
    "YAR_SIGNING_STORE_FILE",
    "YAR_SIGNING_STORE_PASSWORD",
    "YAR_SIGNING_KEY_ALIAS",
    "YAR_SIGNING_KEY_PASSWORD",
)
val previewBuild = providers.gradleProperty("yarPreview").map { it == "true" }.orElse(false)
val releaseCredentials = signingVariables.associateWith { providers.environmentVariable(it).orNull }
val hasReleaseCredentials = releaseCredentials.values.any { !it.isNullOrBlank() }
require(!hasReleaseCredentials || releaseCredentials.values.all { !it.isNullOrBlank() }) {
    "Set all four YAR_SIGNING_* environment variables to enable release signing."
}

android {
    namespace = "io.github.vahidpro.yar"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.vahidpro.yar"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
        manifestPlaceholders["yarAppLabel"] = "@string/app_name"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (hasReleaseCredentials) {
        signingConfigs.create("release") {
            storeFile = file(requireNotNull(releaseCredentials["YAR_SIGNING_STORE_FILE"]))
            storePassword = releaseCredentials["YAR_SIGNING_STORE_PASSWORD"]
            keyAlias = releaseCredentials["YAR_SIGNING_KEY_ALIAS"]
            keyPassword = releaseCredentials["YAR_SIGNING_KEY_PASSWORD"]
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = if (previewBuild.get()) ".preview" else ".debug"
            versionNameSuffix = if (previewBuild.get()) "-preview" else "-debug"
            if (previewBuild.get()) manifestPlaceholders["yarAppLabel"] = "@string/app_preview_name"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            if (hasReleaseCredentials) signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":feature:hokm"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.datastore.preferences)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.android.test.junit)
    androidTestImplementation(libs.android.test.runner)
    debugImplementation(libs.compose.ui.test.manifest)
}
