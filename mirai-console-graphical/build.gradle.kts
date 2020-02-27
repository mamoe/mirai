plugins {
    id("kotlinx-serialization")
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("kotlin")
    id("java")
}

javafx {
    version = "13.0.2"
    modules = listOf("javafx.controls")
    //mainClassName = "Application"
}

apply(plugin = "com.github.johnrengelman.shadow")

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
    runtimeOnly(files("../mirai-core-qqandroid/build/classes/kotlin/jvm/main"))
    api(group = "no.tornado", name = "tornadofx", version = "1.7.19")
    api(group = "com.jfoenix", name = "jfoenix", version = "9.0.8")
    api("org.bouncycastle:bcprov-jdk15on:1.64")
    // classpath is not set correctly by IDE
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}