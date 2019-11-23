@file:Suppress("UNUSED_VARIABLE")

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

fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.kotlinx(id: String, version: String) {
    implementation("org.jetbrains.kotlinx:$id:$version")
}

fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.ktor(id: String, version: String = ktorVersion) {
    implementation("io.ktor:$id:$version")
}

kotlin {


    sourceSets["main"].apply {
        dependencies {
            implementation(project(":mirai-core"))

            kotlin("kotlin-stdlib-jdk8", kotlinVersion)
            kotlin("kotlin-stdlib-jdk7", kotlinVersion)
            kotlin("kotlin-reflect", kotlinVersion)

            ktor("ktor-server-cio")
            kotlinx("kotlinx-io-jvm", kotlinXIoVersion)
            ktor("ktor-http-jvm")
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
            kotlin("kotlin-stdlib", kotlinVersion)
            kotlin("kotlin-serialization", kotlinVersion)

            kotlinx("atomicfu", atomicFuVersion)
            kotlinx("kotlinx-io", kotlinXIoVersion)
            kotlinx("kotlinx-coroutines-io", coroutinesIoVersion)
            kotlinx("kotlinx-coroutines-core", coroutinesVersion)
            kotlinx("kotlinx-serialization-runtime", serializationVersion)
            ktor("ktor-server-core")
            ktor("ktor-http")
        }
    }
}
