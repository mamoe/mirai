@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    id("kotlinx-serialization")
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4-jetbrains-3"
}

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext


val ktorVersion: String by rootProject.ext

val serializationVersion: String by rootProject.ext

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"


description = "QQ protocol library"

val isAndroidSDKAvailable: Boolean by project

val miraiVersion: String by project
version = miraiVersion

kotlin {
    if (isAndroidSDKAvailable) {
        apply(from = rootProject.file("gradle/android.gradle"))
        android("android") {
            publishAllLibraryVariants()
        }
    } else {
        println(
            """Android SDK 可能未安装.
                $name 的 Android 目标编译将不会进行. 
                这不会影响 Android 以外的平台的编译.
            """.trimIndent()
        )
        println(
            """Android SDK might not be installed.
                Android target of $name will not be compiled. 
                It does no influence on the compilation of other platforms.
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
                api(project(":mirai-core"))

                api(kotlin("stdlib", kotlinVersion))

                api("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
                api(kotlinx("coroutines-io", coroutinesIoVersion))
                api(kotlinx("coroutines-core", coroutinesVersion))
            }
        }
        commonMain {
            dependencies {
                api(kotlinx("serialization-runtime-common", serializationVersion))
                api(kotlinx("serialization-protobuf-common", serializationVersion))
            }
        }
        commonTest {
            dependencies {
                api(kotlin("test-annotations-common"))
                api(kotlin("test-common"))
                implementation(kotlin("script-runtime"))
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependencies {
                    api(kotlinx("serialization-protobuf", serializationVersion))
                }
            }

            val androidTest by getting {
                dependencies {
                    implementation(kotlin("test", kotlinVersion))
                    implementation(kotlin("test-junit", kotlinVersion))
                    implementation(kotlin("test-annotations-common"))
                    implementation(kotlin("test-common"))
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
                api(kotlinx("serialization-runtime", serializationVersion))
                api(kotlinx("serialization-protobuf", serializationVersion))
            }
        }

        val jvmTest by getting {
            dependencies {
                api(kotlin("test", kotlinVersion))
                api(kotlin("test-junit", kotlinVersion))
                implementation("org.pcap4j:pcap4j-distribution:1.8.2")

                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions.jvmTarget = "1.8"
//}

apply(from = rootProject.file("gradle/publish.gradle"))
