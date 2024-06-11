// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    //
}

buildscript {
    dependencies {
        //noinspection UseTomlInstead
        classpath("com.android.tools.build:gradle:8.3.2")
        //noinspection UseTomlInstead
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        //noinspection GradleDependency,UseTomlInstead
        classpath("com.google.gms:google-services:4.3.0")
    }
}