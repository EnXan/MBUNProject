plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.4" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin) // Aktualisiere die Version
        classpath ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

tasks.withType<Test> {
    testLogging {
        events("passed", "failed", "skipped", "standardOut", "standardError")
        showStandardStreams = true
    }
}