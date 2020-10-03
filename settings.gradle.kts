pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        google()
        maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}

rootProject.name = "mirai"

include(":mirai-core-api")
include(":mirai-core")
include(":mirai-core-all")