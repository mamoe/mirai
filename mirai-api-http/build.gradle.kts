@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    id("kotlinx-atomicfu")
    kotlin("jvm")
    id("kotlinx-serialization")
}

group = "net.mamoe.mirai"
version = rootProject.ext["mirai_version"].toString()

description = "Mirai Http Api"

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

val serializationVersion: String by rootProject.ext

fun KotlinDependencyHandler.kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun KotlinDependencyHandler.ktor(id: String, version: String = ktorVersion) = "io.ktor:ktor-$id:$version"

kotlin {


    sourceSets["main"].apply {
        dependencies {
            implementation(project(":mirai-core-timpc"))

            implementation(kotlin("stdlib-jdk8", kotlinVersion))
            implementation(kotlin("stdlib-jdk7", kotlinVersion))
            implementation(kotlin("reflect", kotlinVersion))

            implementation(ktor("server-cio"))
            implementation(kotlinx("io-jvm", kotlinXIoVersion))
            implementation(ktor("http-jvm"))
        }
    }

    sourceSets["test"].apply {
        dependencies {
        }
        kotlin.outputDir = file("build/classes/kotlin/jvm/test")
        kotlin.setSrcDirs(listOf("src/$name/kotlin"))

    }

    sourceSets.all {
        languageSettings.enableLanguageFeature("InlineClasses")
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")

        dependencies {
            implementation(kotlin("stdlib", kotlinVersion))
            implementation(kotlin("serialization", kotlinVersion))

            implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
            implementation(kotlinx("io", kotlinXIoVersion))
            implementation(kotlinx("coroutines-io", coroutinesIoVersion))
            implementation(kotlinx("coroutines-core", coroutinesVersion))
            implementation(kotlinx("serialization-runtime", serializationVersion))
            implementation(ktor("server-core"))
            implementation(ktor("http"))
        }
    }
}
