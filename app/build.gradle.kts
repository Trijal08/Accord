@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    // Corrected to use a readProperties function result which is not a Project property
    val releaseType = readProperties(file("../package.properties")).getProperty("releaseType")
    if (releaseType.contains("\"")) {
        throw IllegalArgumentException("releaseType must not contain \"")
    }

    namespace = ""uk.akane.accord""
    compileSdk = 36

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "uk.akane.accord"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
        prefab = true
    }

    packaging {
        jniLibs {
            // https://issuetracker.google.com/issues/168777344#comment11
            pickFirsts += "lib/arm64-v8a/libdlfunc.so"
            pickFirsts += "lib/armeabi-v7a/libdlfunc.so"
            pickFirsts += "lib/x86/libdlfunc.so"
            pickFirsts += "lib/x86_64/libdlfunc.so"
        }
        dex {
            useLegacyPackaging = false
        }
        resources {
            // https://stackoverflow.com/a/58956288
            excludes += "META-INF/*.version"
            // https://github.com/Kotlin/kotlinx.coroutines?tab=readme-ov-file#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes += "DebugProbesKt.bin"
            // https://issueantenna.com/repo/kotlin/kotlinx.coroutines/issues/3158
            excludes += "kotlin-tooling-metadata.json"

            excludes += "META-INF/**/LICENSE.txt"
        }
    }

    defaultConfig {
        applicationId = "uk.akane.accord"
        // Reasons to not support KK include me.zhanghai.android.fastscroll, WindowInsets for
        // bottom sheet padding, ExoPlayer requiring multidex for KK and poor SD card support
        // That said, supporting Android 5.0 barely costs any tech debt and we plan to keep support
        // for it for a while.
        // Bye bye android 12 - cuz blur
        minSdk = 31
        targetSdk = 36
        versionCode = 18
        versionName = "beta2"
        buildConfigField(
            "String",
            "MY_VERSION_NAME",
            "\"Beta 2\""
        )
        buildConfigField(
            "String",
            "RELEASE_TYPE",
            "\"$releaseType\""
        )
        
        // REMOVED: setProperty("archivesBaseName", "Accord-$versionName")
        // archivesBaseName is deprecated and no longer supported in Gradle 9
    }

    signingConfigs {
        create("release") {
            if (project.hasProperty("AKANE_RELEASE_KEY_ALIAS")) {
                // Corrected to use provider for lazy evaluation and compatibility
                storeFile = project.properties["AKANE_RELEASE_STORE_FILE"]?.let { file(it.toString()) }
                storePassword = project.properties["AKANE_RELEASE_STORE_PASSWORD"]?.toString()
                keyAlias = project.properties["AKANE_RELEASE_KEY_ALIAS"]?.toString()
                keyPassword = project.properties["AKANE_RELEASE_KEY_PASSWORD"]?.toString()
            }
        }
    }

    splits.abi {
        // Enables building multiple APKs per ABI.
        isEnable = true

        // By default all ABIs are included, so use reset() and include to specify that you only
        // want APKs for x86 and x86_64.
        // Resets the list of ABIs for Gradle to create APKs for to none.
        reset()

        // Specifies a list of ABIs for Gradle to create APKs for.
        include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

        // Specifies that you don't want to also generate a universal APK that includes all ABIs.
        isUniversalApk = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

dependencies {
    implementation(project(":libPhonograph"))
    implementation(project(":Cupertino"))
    implementation(project(":hificore"))
    implementation(project(":misc:alacdecoder"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.midi)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.hiddenapibypass)
    implementation(libs.material)
    implementation(libs.coil)
    debugImplementation(libs.leakcanary.android)
}
