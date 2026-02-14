pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Menggunakan sintaks Kotlin DSL yang benar
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    // Memastikan repositori ini digunakan oleh semua modul aplikasi
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "CloudstreamPlugins"

// Pastikan folder plugin Anda terdaftar di sini
// Jika foldernya bernama 'app', gunakan:
include(":app")
