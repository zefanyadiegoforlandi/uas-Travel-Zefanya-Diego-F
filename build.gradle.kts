buildscript {
    val navVersion = "2.7.6"
    val kotlinVersion by extra("2.0.0-Beta1")
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
    }
    repositories {
        mavenCentral()
        google()
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.6.0" apply false
}