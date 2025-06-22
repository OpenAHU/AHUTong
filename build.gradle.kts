//// Top-level build file where you can add configuration options common to all sub-projects/modules.
//buildscript {
//    ext {
//        kotlinVersion = "1.7.21"
//        lifecycleVersion = "2.5.1"
//        androidxCore = "1.9.0"
//        appCompat = "1.5.1"
//        googleMaterial = "1.7.0"
//        navVersion = "2.5.3"
//        sdkVersion = 33
//    }
//
//    repositories {
//        google()
//        mavenCentral()
//        maven { url "https://jitpack.io" }
//    }
//    dependencies {
//        classpath 'com.android.tools.build:gradle:7.3.1'
//        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
//
//        // NOTE: Do not place your application dependencies here; they belong
//        // in the individual module build.gradle files
//    }
//}
//
//
//task clean(type: Delete) {
//    delete rootProject.buildDir
//}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}


buildscript{
    repositories {
        google()
        mavenCentral()
    }
}
