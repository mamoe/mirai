@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("signing")
    `maven-publish`
    id("com.jfrog.bintray") version Versions.Publishing.bintray
}

description = "Mirai API module"

val isAndroidSDKAvailable: Boolean by project

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

    jvm() {
        // withJava() // https://youtrack.jetbrains.com/issue/KT-39991
    }

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiExperimentalAPI")
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.LowLevelAPI")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            languageSettings.useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
            languageSettings.progressiveMode = true
        }

        commonMain {
            dependencies {
                api(kotlin("stdlib"))
                api(kotlin("serialization"))
                api(kotlin("reflect"))

                api(kotlinx("serialization-runtime-common", Versions.Kotlin.serialization))
                api(kotlinx("serialization-protobuf-common", Versions.Kotlin.serialization))
                api(kotlinx("io", Versions.Kotlin.io))
                api(kotlinx("coroutines-io", Versions.Kotlin.coroutinesIo))
                api(kotlinx("coroutines-core-common", Versions.Kotlin.coroutines))

                api("org.jetbrains.kotlinx:atomicfu-common:${Versions.Kotlin.atomicFU}")

                api(ktor("client-cio", Versions.Kotlin.ktor))
                api(ktor("client-core", Versions.Kotlin.ktor))
                api(ktor("network", Versions.Kotlin.ktor))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-annotations-common"))
                implementation(kotlin("test-common"))
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependencies {
                    api(kotlin("reflect"))

                    api(kotlinx("io-jvm", Versions.Kotlin.io))
                    api(kotlinx("serialization-runtime", Versions.Kotlin.serialization))
                    api(kotlinx("serialization-protobuf", Versions.Kotlin.serialization))
                    api(kotlinx("coroutines-io-jvm", Versions.Kotlin.coroutinesIo))
                    api(kotlinx("coroutines-core", Versions.Kotlin.coroutines))

                    api(ktor("client-android", Versions.Kotlin.ktor))
                }
            }

            val androidTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(kotlin("test-junit"))
                    implementation(kotlin("test-annotations-common"))
                    implementation(kotlin("test-common"))
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                //api(kotlin("stdlib-jdk8"))
                //api(kotlin("stdlib-jdk7"))
                api(kotlin("reflect"))

                api(ktor("client-core-jvm", Versions.Kotlin.ktor))
                api(kotlinx("io-jvm", Versions.Kotlin.io))
                api(kotlinx("serialization-runtime", Versions.Kotlin.serialization))
                api(kotlinx("serialization-protobuf", Versions.Kotlin.serialization))
                api(kotlinx("coroutines-io-jvm", Versions.Kotlin.coroutinesIo))
                api(kotlinx("coroutines-core", Versions.Kotlin.coroutines))

                api("org.bouncycastle:bcprov-jdk15on:1.64")
                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.pcap4j:pcap4j-distribution:1.8.2")

                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}

apply(from = rootProject.file("gradle/publish.gradle"))
