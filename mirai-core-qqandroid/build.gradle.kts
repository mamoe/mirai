@file:Suppress("UNUSED_VARIABLE")

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
version = rootProject.ext.get("mirai_version")!!.toString()

kotlin {
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

    jvm("jvm") {
    }

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")

            dependencies {
                api(project(":mirai-core"))

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
            }
        }
        commonTest {
            dependencies {
                api(kotlin("test-annotations-common"))
                api(kotlin("test-common"))
            }
        }

        val androidMain by getting {
            dependencies {
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
            }
        }

        val jvmTest by getting {
            dependencies {
                api(kotlin("test", kotlinVersion))
                api(kotlin("test-junit", kotlinVersion))
                implementation("org.pcap4j:pcap4j-distribution:1.8.2")

                //runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}