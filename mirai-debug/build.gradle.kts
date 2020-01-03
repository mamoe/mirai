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
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

dependencies {

    runtimeOnly(files("../mirai-core-timpc/build/classes/kotlin/jvm/main")) // IDE bug
    implementation(project(":mirai-core-timpc"))
    // runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main")) // classpath is not added correctly by IDE

    implementation("org.bouncycastle:bcprov-jdk15:1.46")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("org.pcap4j:pcap4j-distribution:1.8.2")
    implementation("no.tornado:tornadofx:1.7.17")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = "1.3.2")

    implementation(kotlin("stdlib", kotlinVersion))
    implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
    implementation(kotlinx("io-jvm", kotlinXIoVersion))
    implementation(kotlinx("io", kotlinXIoVersion))
    implementation(kotlinx("coroutines-io", coroutinesIoVersion))
    implementation(kotlinx("coroutines-core", coroutinesVersion))

    implementation(kotlinx("serialization-runtime", serializationVersion))


    implementation(ktor("http-cio", ktorVersion))
    implementation(ktor("http", ktorVersion))
    implementation(ktor("client-core-jvm", ktorVersion))
    implementation(ktor("client-cio", ktorVersion))
    implementation(ktor("client-core", ktorVersion))
    implementation(ktor("network", ktorVersion))

}