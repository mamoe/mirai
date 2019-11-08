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

val kotlinVersion = rootProject.ext["kotlin_version"].toString()
val atomicFuVersion = rootProject.ext["atomicfu_version"].toString()
val coroutinesVersion = rootProject.ext["coroutines_version"].toString()
val kotlinXIoVersion = rootProject.ext["kotlinxio_version"].toString()
val coroutinesIoVersion = rootProject.ext["coroutinesio_version"].toString()

val klockVersion = rootProject.ext["klock_version"].toString()
val ktorVersion = rootProject.ext["ktor_version"].toString()

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

dependencies {
    api(project(":mirai-core"))
    runtimeOnly(files("../../mirai-core/build/classes/kotlin/jvm/main")) // mpp targeting android limitation

    api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("org.pcap4j:pcap4j-distribution:1.8.2")
    implementation("no.tornado:tornadofx:1.7.17")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = "1.3.2")
    implementation(files("./lib/jpcap.jar"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinXIoVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-io:$kotlinXIoVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-io:$coroutinesIoVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}