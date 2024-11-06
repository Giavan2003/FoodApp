pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("libs") // Specify the directory where the AAR file is located
        }
        maven { url = uri("https://jitpack.io") } // Thêm repository Maven tùy chỉnh
    }
}

rootProject.name = "FoodApp"
include(":app")
