plugins {
    id("kotlinx-serialization")
    id("kotlin")
    id("java")
}


apply(plugin = "com.github.johnrengelman.shadow")

version = Versions.Mirai.console

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.MiraiConsoleTerminalLoader"
    }
}

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

val serializationVersion: String by rootProject.ext

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"


val miraiVersion =  Versions.Mirai.core

dependencies {
    implementation("net.mamoe:mirai-core-jvm:$miraiVersion")
    implementation("net.mamoe:mirai-core-qqandroid-jvm:$miraiVersion")
    api(project(":mirai-console"))

    api(group = "com.googlecode.lanterna", name = "lanterna", version = "3.0.2")
}