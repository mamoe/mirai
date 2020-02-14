plugins {
    id("kotlinx-serialization")
    id("kotlin")
    id("java")
}


apply(plugin = "com.github.johnrengelman.shadow")

apply(plugin = "java-library")

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.MiraiConsoleLoader"
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



dependencies {
    api(project(":mirai-core"))
    api(project(":mirai-core-qqandroid"))
    api(project(":mirai-api-http"))
    runtimeOnly(files("../mirai-core-qqandroid/build/classes/kotlin/jvm/main"))
    runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main"))
    api(kotlin("serialization"))
    api(group = "com.alibaba", name = "fastjson", version = "1.2.62")
    // classpath is not set correctly by IDE
}