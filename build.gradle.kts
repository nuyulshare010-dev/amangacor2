import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        // Menggunakan commit hash spesifik agar build stabil
        classpath("com.github.recloudstream:gradle:cc41b8d84d")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    }
}

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

android {
    compileSdk = 34
    namespace = "com.example.yourplugin" // Sesuaikan dengan package Anda

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Gunakan pre-release atau master-SNAPSHOT untuk library
    val cloudstreamVersion = "master-SNAPSHOT" 
    compileOnly("com.github.recloudstream:cloudstream:$cloudstreamVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
    }
}
