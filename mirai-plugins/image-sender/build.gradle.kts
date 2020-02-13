plugins {
    kotlin("jvm")
    java
    id("com.github.johnrengelman.shadow") 
}

version = "1.0.0"

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext
val serializationVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

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
    api(project(":mirai-console"))
    runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main")) // classpath is not added correctly by IDE

    api(kotlin("stdlib", kotlinVersion))
    api(kotlinx("io-jvm", kotlinXIoVersion))
    api(kotlinx("io", kotlinXIoVersion))
    api(kotlinx("coroutines-io", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))
    api("org.jsoup:jsoup:1.12.1")
    api(group = "com.alibaba", name = "fastjson", version = "1.2.62")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}