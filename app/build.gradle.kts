import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.stefansundin.sshremote"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.stefansundin.sshremote"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        base.archivesName = "ssh-remote-$versionName"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "JSCH_VERSION", "\"${libs.versions.jsch.get()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Fix for "Unsupported class file major version 68" issue after upgrading to kotlin 2.3.0
composeCompiler {
    includeComposeMappingFile.set(false)
}

dependencies {
    // Core and Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // --- Desugaring to support newer Java features ---
    coreLibraryDesugaring(libs.desugar)

    // --- Compose BOM (Bill of Materials) ---
    // This correctly manages versions for all the Compose libraries below.
    implementation(platform(libs.androidx.compose.bom))

    // --- Compose UI Libraries ---
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended.android)

    // --- ViewModel ---
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- Database ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.androidx.room.compiler)

    // --- SSH ---
    implementation(libs.jsch)
    implementation(libs.bouncycastle.bcprov.jdk18on) // BouncyCastle is required for some SSH features on older Java versions
    // Used for Ed25519 key generation:
    implementation(libs.bouncycastle.bcpkix.jdk18on)
    implementation(libs.eddsa)

    // --- JSON Serialization ---
    implementation(libs.gson)

    // --- QR Code Scanning ---
    implementation(libs.zxing)

    // --- Markdown ---
    implementation(libs.boswelja.compose.core)
    implementation(libs.boswelja.compose.markdown)

    // --- Testing Dependencies ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // The BOM is also needed here for test-specific Compose dependencies.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // --- Debugging Dependencies ---
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
