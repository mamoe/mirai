plugins {
    id("kotlin")
}

apply(plugin = "com.github.johnrengelman.shadow")

val kotlinVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext

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

val serializationVersion: String by rootProject.ext

dependencies {
    api(kotlin("stdlib", kotlinVersion))

    api(kotlinx("coroutines-core", coroutinesVersion))

    api(ktor("client-core-jvm", ktorVersion))
    api(ktor("client-cio", ktorVersion))
    api(kotlin("reflect"))

    api(group = "com.alibaba", name = "fastjson", version = "1.2.62")
    api(group = "org.yaml", name = "snakeyaml", version = "1.25")
    api(group = "com.moandjiezana.toml", name = "toml4j", version = "0.7.2")


    api(kotlin("stdlib", kotlinVersion))
    api(kotlin("serialization", kotlinVersion))

    api(kotlin("reflect", kotlinVersion))

    api(kotlinx("coroutines-io-jvm", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))
    api(kotlinx("serialization-runtime", serializationVersion))
    api("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")

    api("org.bouncycastle:bcprov-jdk15on:1.64")

    api(ktor("http-cio", ktorVersion))
    api(ktor("http-jvm", ktorVersion))
    api(ktor("io-jvm", ktorVersion))
    api(ktor("client-core-jvm", ktorVersion))
    api(ktor("client-cio", ktorVersion))
    api(ktor("network", ktorVersion))
}

val miraiConsoleWrapperVersion: String by project.ext
version = miraiConsoleWrapperVersion

description = "Console with plugin support for mirai"