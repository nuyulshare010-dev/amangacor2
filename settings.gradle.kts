pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url 'https://jitpack.io' } // Tambahkan ini agar plugin ditemukan
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' } // Tambahkan ini untuk library
    }
}

rootProject.name = "CloudstreamPlugins"
include ':app' // Sesuaikan dengan nama modul Anda, biasanya ':app' atau ':plugin'
