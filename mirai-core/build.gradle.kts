@file:Suppress("UNUSED_VARIABLE")

import java.util.*

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    id("com.android.library")
    id("kotlinx-serialization")
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4-jetbrains-3" // DO NOT CHANGE THIS VERSION UNLESS YOU WANT TO WASTE YOUR TIME
}

apply(from = rootProject.file("gradle/publish.gradle"))

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

val serializationVersion: String by rootProject.ext

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"


description = "QQ protocol library"

kotlin {
    val keyProps = Properties()
    val keyFile = file("../local.properties")
    if (keyFile.exists()) keyProps.load(keyFile.inputStream())
    if (keyProps.getProperty("sdk.dir", "").isNotEmpty()) {
        android("android") {
            publishAllLibraryVariants()
            project.android {
                compileSdkVersion(29)

                defaultConfig {
                    minSdkVersion(15)
                }

                // sourceSets.filterIsInstance(com.android.build.gradle.api.AndroidSourceSet::class.java).forEach {
                //     it.manifest.srcFile("src/androidMain/res/AndroidManifest.xml")
                //     it.res.srcDirs(file("src/androidMain/res"))
                // }
                //(sourceSets["main"] as AndroidSourceSet).java.srcDirs(file("src/androidMain/kotlin"))
            }
        }
    } else {
        println(
            """Android SDK 可能未安装.
                $name 的 Android 目标编译将不会进行. 
                这不会影响 Android 以外的平台的编译, 因此 JVM 等平台相关的编译和测试均能正常进行.
            """.trimIndent()
        )
    }

    jvm("jvm") {
    }

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")

            dependencies {
                api(kotlin("stdlib", kotlinVersion))
                api(kotlin("serialization", kotlinVersion))

                api("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
                api(kotlinx("io", kotlinXIoVersion))
                api(kotlinx("coroutines-io", coroutinesIoVersion))
                api(kotlinx("coroutines-core", coroutinesVersion))
            }
        }
        commonMain {
            dependencies {
                api(kotlin("reflect", kotlinVersion))
                api(kotlin("serialization", kotlinVersion))
                api(kotlinx("coroutines-core-common", coroutinesVersion))
                api(kotlinx("serialization-runtime-common", serializationVersion))

                api(ktor("http-cio", ktorVersion))
                api(ktor("http", ktorVersion))
                api(ktor("client-core-jvm", ktorVersion))
                api(ktor("client-cio", ktorVersion))
                api(ktor("client-core", ktorVersion))
                api(ktor("network", ktorVersion))
                //implementation("io.ktor:ktor-io:1.3.0-beta-1")

                runtimeOnly(files("build/classes/kotlin/metadata/main")) // classpath is not properly set by IDE
            }
        }
        commonTest {
            dependencies {
                api(kotlin("test-annotations-common"))
                api(kotlin("test-common"))

                runtimeOnly(files("build/classes/kotlin/metadata/test")) // classpath is not properly set by IDE
            }
        }

        val androidMain by getting {
            dependencies {
                api(kotlin("reflect", kotlinVersion))

                api(kotlinx("serialization-runtime", serializationVersion))
                api(kotlinx("coroutines-android", coroutinesVersion))

                api(ktor("client-android", ktorVersion))
            }
        }

        val androidTest by getting {
            dependencies {
                api(kotlin("test", kotlinVersion))
                api(kotlin("test-junit", kotlinVersion))
                api(kotlin("test-annotations-common"))
                api(kotlin("test-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                //api(kotlin("stdlib-jdk8", kotlinVersion))
                //api(kotlin("stdlib-jdk7", kotlinVersion))
                api(kotlin("reflect", kotlinVersion))

                api(ktor("client-core-jvm", ktorVersion))
                api(kotlinx("io-jvm", kotlinXIoVersion))
                api(kotlinx("serialization-runtime", serializationVersion))

                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
            }
        }

        val jvmTest by getting {
            dependencies {
                api(kotlin("test", kotlinVersion))
                api(kotlin("test-junit", kotlinVersion))
                implementation("org.pcap4j:pcap4j-distribution:1.8.2")

                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}