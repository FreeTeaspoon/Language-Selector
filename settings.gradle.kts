pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "language_selector"
include(":app")
include(":hidden_api")

includeBuild("third_party/miuix") {
    dependencySubstitution {
        substitute(module("top.yukonga.miuix.kmp:miuix-blur-android"))
            .using(project(":miuix-blur"))
        substitute(module("top.yukonga.miuix.kmp:miuix-ui"))
            .using(project(":miuix-ui"))
        substitute(module("top.yukonga.miuix.kmp:miuix-ui-android"))
            .using(project(":miuix-ui"))
        substitute(module("top.yukonga.miuix.kmp:miuix-shader-android"))
            .using(project(":miuix-shader"))
        substitute(module("top.yukonga.miuix.kmp:miuix-nav"))
            .using(project(":miuix-nav"))
    }
}
