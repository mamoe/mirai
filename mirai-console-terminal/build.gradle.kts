plugins {
    id("kotlinx-serialization")
    id("kotlin")
    id("java")
}


apply(plugin = "com.github.johnrengelman.shadow")


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


val mirai_version: String by rootProject.ext

dependencies {
    implementation("net.mamoe:mirai-core-jvm:$mirai_version")
    implementation("net.mamoe:mirai-core-qqandroid-jvm:$mirai_version")

    // api(project(":mirai-api-http"))
    api(project(":mirai-console"))
    api(group = "com.googlecode.lanterna", name = "lanterna", version = "3.0.2")
    api("org.bouncycastle:bcprov-jdk15on:1.64")
    // classpath is not set correctly by IDE
}