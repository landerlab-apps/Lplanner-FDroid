// Lplanner for F-Droid.
//
// A deliberately separate project from DeveloperAndroid/Lplanner-Android, which
// builds the Google Play release. The two can be updated, held back or
// abandoned independently. What they must never do is disagree about a
// schedule, and they cannot: both compile the same czplan.c out of ZPlanKit.
//
// Nothing here is Play-specific. There is no signing config, because F-Droid
// builds this on their own servers and signs it with their key — a keystore in
// this tree would be ignored at best and a mistake at worst.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// The shared engine. Default is the `engine` git submodule at the root of this
// repository, which is what F-Droid's builders check out and compile.
//
// Resolved against rootProject, NOT against this module. project.file() would
// resolve "engine" to app/engine, and the submodule is a level above that, so
// the F-Droid build would have failed at configure time with the engine
// sitting right there in the tree. Relative paths here mean "from the
// repository root", which is where a reader would look for them.
val zplanKitDir: String = (project.findProperty("zplankit.dir") as String? ?: "engine")
    .let { rootProject.file(it).canonicalPath }

android {
    namespace = "com.landerlab.lplanner"
    compileSdk = 36
    ndkVersion = "27.2.12479018"

    defaultConfig {
        // Suffixed, and not negotiable. Google Play signs its artefacts with a
        // key Google holds; F-Droid signs with F-Droid's. Two certificates
        // under one application id means Android refuses to install either
        // build over the other, so a diver moving between stores would have to
        // uninstall first — losing his plan log and his carried tissue
        // loading. The suffix costs an untidy package name and buys
        // side-by-side installation.
        applicationId = "com.landerlab.lplanner.fdroid"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "1.6.0"

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DZPLANKIT_DIR=$zplanKitDir",
                    // 16 KB page size support. Required by Play for apps targeting
                    // Android 15+. NDK r27 needs this opt-in; r28+ does it by default,
                    // where the flag is simply a no-op.
                    "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON",
                )
                cFlags += listOf("-std=c99", "-O2")
            }
        }
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // No signingConfig. F-Droid signs the artefact it builds.
        }
        debug { isMinifyEnabled = false }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

    buildFeatures { compose = true; buildConfig = true }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        // Native libs must be stored uncompressed and page-aligned in the APK for
        // 16 KB devices to mmap them directly. Default on modern AGP; pinned here
        // so a future change can't silently break the alignment check.
        jniLibs.useLegacyPackaging = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    // Explicit upgrade, not a new feature: compose ui-graphics pulls graphics-path
    // 1.0.1 transitively, whose .so is 4 KB-aligned and fails Play's 16 KB check.
    implementation(libs.androidx.graphics.path)
    debugImplementation(libs.androidx.ui.tooling)
}
