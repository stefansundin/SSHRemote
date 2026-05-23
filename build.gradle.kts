// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.parcelize) apply false

    // https://github.com/google/ksp/releases
    id("com.google.devtools.ksp") version "2.3.8" apply false

    // https://developer.android.com/jetpack/androidx/releases/room
    id("androidx.room") version "2.8.4" apply false
}
