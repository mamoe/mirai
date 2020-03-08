plugins {
    id("kotlin")
}

apply(plugin = "com.github.johnrengelman.shadow")

val kotlinVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext

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
    //core && protocol
    api(kotlin("stdlib", kotlinVersion))
    api(kotlin("serialization", kotlinVersion))
    api(kotlin("reflect", kotlinVersion))

    api(kotlinx("coroutines-core-common", coroutinesVersion))
    api(kotlinx("serialization-runtime-common", serializationVersion))
    api(kotlinx("serialization-protobuf-common", serializationVersion))
    api(kotlinx("io", kotlinXIoVersion))
    api(kotlinx("coroutines-io", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))

    api("org.jetbrains.kotlinx:atomicfu-common:$atomicFuVersion")

    api(ktor("client-cio", ktorVersion))
    api(ktor("client-core", ktorVersion))
    api(ktor("network", ktorVersion))
    api(kotlin("reflect", kotlinVersion))

    api(ktor("client-core-jvm", ktorVersion))
    api(kotlinx("io-jvm", kotlinXIoVersion))
    api(kotlinx("serialization-runtime", serializationVersion))
    api(kotlinx("serialization-protobuf", serializationVersion))
    api(kotlinx("coroutines-io-jvm", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))

    api("org.bouncycastle:bcprov-jdk15on:1.64")

    api("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
    api(kotlinx("serialization-runtime-common", serializationVersion))
    api(kotlinx("serialization-protobuf-common", serializationVersion))
    api(kotlinx("serialization-runtime", serializationVersion))

    //for slf4j[ktor used]
   // api(group = "org.apache.cassandra", name = "cassandra-all", version = "0.8.1")

    //mirai-console
    api(group = "com.alibaba", name = "fastjson", version = "1.2.62")
    api(group = "org.yaml", name = "snakeyaml", version = "1.25")
    api(group = "com.moandjiezana.toml", name = "toml4j", version = "0.7.2")

}

val miraiConsoleWrapperVersion: String by project.ext
version = miraiConsoleWrapperVersion

description = "Console with plugin support for mirai"