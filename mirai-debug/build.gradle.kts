plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.8"
    kotlin("jvm")
    java
}

javafx {
    version = "11"
    modules = listOf("javafx.controls")
    //mainClassName = "Application"
}

application {
    mainClassName = "Application"
}

val kotlinVersion = rootProject.ext["kotlinVersion"].toString()
val atomicFuVersion = rootProject.ext["atomicFuVersion"].toString()
val coroutinesVersion = rootProject.ext["coroutinesVersion"].toString()
val kotlinXIoVersion = rootProject.ext["kotlinXIoVersion"].toString()
val coroutinesIoVersion = rootProject.ext["coroutinesIoVersion"].toString()

val klockVersion = rootProject.ext["klockVersion"].toString()
val ktorVersion = rootProject.ext["ktorVersion"].toString()

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

dependencies {
    api(project(":mirai-core"))
    runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main")) // mpp targeting android limitation

    api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("org.pcap4j:pcap4j-distribution:1.8.2")
    implementation("no.tornado:tornadofx:1.7.17")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = "1.3.2")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinXIoVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-io:$kotlinXIoVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-io:$coroutinesIoVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}