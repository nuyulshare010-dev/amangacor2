import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        // Wajib ada di sini agar plugin Cloudstream ditemukan saat inisialisasi
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        // Menggunakan master-SNAPSHOT karena commit hash spesifik sebelumnya gagal di-resolve oleh JitPack
        classpath("com.github.recloudstream:gradle:master-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    }
}

// Menerapkan plugin
apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

android {
    // Sesuaikan compileSdk dengan yang Anda gunakan, 34 adalah standar terbaru
    compileSdk = 34

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

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

dependencies {
    // Library utama Cloudstream untuk kompilasi plugin
    val cloudstreamVersion = "master-SNAPSHOT"
    compileOnly("com.github.recloudstream:cloudstream:$cloudstreamVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        // Menangani kompatibilitas interface Kotlin
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
    }
}

// Konfigurasi Cloudstream Extension
configure<CloudstreamExtension> {
    // Anda bisa mengosongkan ini atau mengisi metadata plugin jika diperlukan
}
