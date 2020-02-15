plugins {
    kotlin("jvm")
    java
}

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext
val serializationVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

description = "Java helper for Mirai"

@Suppress("PropertyName")
val mirai_japt_version: String by rootProject.ext
version = mirai_japt_version

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")

            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

dependencies {
    api(project(":mirai-core"))
    runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main")) // classpath is not added correctly by IDE

    api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = "1.3.2")

    api(kotlin("stdlib", kotlinVersion))
    api(kotlinx("io-jvm", kotlinXIoVersion))
    api(kotlinx("io", kotlinXIoVersion))
    api(kotlinx("coroutines-io", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))
    api(kotlin("stdlib-jdk7", kotlinVersion))
    api(kotlin("stdlib-jdk8", kotlinVersion))
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}