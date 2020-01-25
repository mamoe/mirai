plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("kotlinx-serialization")
    id("kotlin")
    id("java")
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
    api(project(":mirai-core-timpc"))
    runtimeOnly(files("../mirai-core-timpc/build/classes/kotlin/jvm/main"))
    runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main"))
    api(kotlin("serialization"))
    // classpath is not set correctly by IDE
}