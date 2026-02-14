import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        // Repositori wajib agar Gradle bisa menemukan plugin Cloudstream
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        // Plugin utama untuk build plugin Cloudstream
        classpath("com.github.recloudstream:gradle:master-SNAPSHOT")
        
        // Plugin Kotlin untuk Android
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    }
}

// Menerapkan plugin yang diperlukan
apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

// Konfigurasi untuk memastikan dependensi library juga dicari di JitPack
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

android {
    // Sesuaikan dengan versi SDK yang Anda gunakan di project
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

// Konfigurasi khusus Cloudstream
configure<CloudstreamExtension> {
    // Nama file hasil build .cs3 nantinya
    // Jika Anda ingin nama spesifik, ganti di sini
}

dependencies {
    val cloudstreamVersion = "pre-release" // atau versi spesifik lainnya
    compileOnly("com.github.recloudstream:cloudstream:$cloudstreamVersion")
    
    // Tambahkan dependensi lain yang diperlukan plugin Anda di sini
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
    }
}
