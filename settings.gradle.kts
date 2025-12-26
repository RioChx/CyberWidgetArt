pluginManagement {
    repositories {
        google()                  // Required for Android plugins
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()                  // Required for Android libraries
        mavenCentral()
    }
}

rootProject.name = "CyberWidgetArt"
include(":app")                   // Change if your module is named differently (usually :app)
