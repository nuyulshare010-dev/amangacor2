pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Sintaks Kotlin yang benar menggunakan url = uri("...")
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Memastikan JitPack tersedia untuk semua dependensi library
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "CloudstreamPlugins"

// Pastikan menyertakan semua modul plugin Anda di sini
// Jika folder plugin Anda bernama 'app', biarkan seperti ini:
include(":app")
