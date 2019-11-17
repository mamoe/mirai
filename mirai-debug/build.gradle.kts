plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.8"
    kotlin("jvm")
    java
    id("kotlinx-serialization")
}

javafx {
    version = "11"
    modules = listOf("javafx.controls")
    //mainClassName = "Application"
}

application {
    mainClassName = "Application"
}

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
        }
    }
}

fun DependencyHandlerScope.kotlinx(id: String, version: String) {
    implementation("org.jetbrains.kotlinx:$id:$version")
}

fun DependencyHandlerScope.ktor(id: String, version: String) {
    implementation("io.ktor:$id:$version")
}

dependencies {
    implementation(project(":mirai-core"))
    runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main")) // mpp targeting android limitation

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("org.pcap4j:pcap4j-distribution:1.8.2")
    implementation("no.tornado:tornadofx:1.7.17")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = "1.3.2")

    kotlin("kotlin-stdlib", kotlinVersion)
    kotlinx("atomicfu", atomicFuVersion)
    kotlinx("kotlinx-io-jvm", kotlinXIoVersion)
    kotlinx("kotlinx-io", kotlinXIoVersion)
    kotlinx("kotlinx-coroutines-io", coroutinesIoVersion)
    kotlinx("kotlinx-coroutines-core", coroutinesVersion)

    kotlinx("kotlinx-serialization-runtime", serializationVersion)
}