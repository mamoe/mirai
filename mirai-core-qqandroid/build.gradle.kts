@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    `maven-publish`
    id("com.jfrog.bintray") version Versions.Publishing.bintray
}

description = "Mirai Protocol implementation for QQ Android"

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

    jvm("jvm") {
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

            dependencies {
                api(project(":mirai-core"))
            }
        }

        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib", Versions.Kotlin.stdlib))
                api(kotlinx("serialization-runtime-common", Versions.Kotlin.serialization))
                api(kotlinx("serialization-protobuf-common", Versions.Kotlin.serialization))
                api("moe.him188:jcekt-common:${Versions.jcekt}")
                api("org.jetbrains.kotlinx:atomicfu:${Versions.Kotlin.atomicFU}")
                api(kotlinx("io", Versions.Kotlin.io))
                api(kotlinx("coroutines-io", Versions.Kotlin.coroutinesIo))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-annotations-common"))
                implementation(kotlin("test-common"))
                implementation(kotlin("script-runtime"))
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependencies {
                    api(kotlinx("serialization-protobuf", Versions.Kotlin.serialization))
                }
            }

            val androidTest by getting {
                dependencies {
                    implementation(kotlin("test", Versions.Kotlin.stdlib))
                    implementation(kotlin("test-junit", Versions.Kotlin.stdlib))
                    implementation(kotlin("test-annotations-common"))
                    implementation(kotlin("test-common"))
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
                //    api(kotlinx("coroutines-debug", Versions.Kotlin.coroutines))
                api("moe.him188:jcekt:${Versions.jcekt}")
                api(kotlinx("serialization-runtime", Versions.Kotlin.serialization))
                //api(kotlinx("serialization-protobuf", Versions.Kotlin.serialization))

            }
        }

        val jvmTest by getting {
            dependencies {
                dependsOn(commonTest)
                implementation(kotlin("test", Versions.Kotlin.stdlib))
                implementation(kotlin("test-junit", Versions.Kotlin.stdlib))
                implementation("org.pcap4j:pcap4j-distribution:1.8.2")

                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}

apply(from = rootProject.file("gradle/publish.gradle"))
