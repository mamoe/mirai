plugins {
    id("kotlin")
}

apply(plugin = "com.github.johnrengelman.shadow")

val kotlinVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext

val ktorVersion: String by rootProject.ext

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.wrapper.WrapperMain"
    }
}


val miraiVersion: String by rootProject.ext

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")

            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.useExperimentalAnnotation("kotlin.OptIn")
        }
    }
}
dependencies {
    compileOnly("net.mamoe:mirai-core-jvm:$miraiVersion")

    api(kotlin("stdlib", kotlinVersion))
    api(kotlin("reflect", kotlinVersion))

    api(kotlinx("coroutines-core", coroutinesVersion))

    api(ktor("client-core-jvm", ktorVersion))
    api(ktor("client-cio", ktorVersion))
}

val miraiConsoleWrapperVersion: String by project.ext
version = miraiConsoleWrapperVersion

description = "Console with plugin support for mirai"