// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        val nav_version = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version") // Safe Args
    }
}


plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false // Hilt eklentisi
    id ("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
    id ("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}