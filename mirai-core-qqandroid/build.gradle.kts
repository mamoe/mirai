@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
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

    sourceSets.apply {
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
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.UnstableExternalImage")

            languageSettings.progressiveMode = true

            dependencies {
                api(project(":mirai-core"))
            }
        }

        val commonMain by getting {
            dependencies {
                api(kotlinx("serialization-core", Versions.Kotlin.serialization))
                api(kotlinx("coroutines-core", Versions.Kotlin.coroutines))
                implementation(kotlinx("serialization-protobuf", Versions.Kotlin.serialization))
                api("org.jetbrains.kotlinx:atomicfu:${Versions.Kotlin.atomicFU}")
                implementation(kotlinx("io", Versions.Kotlin.io)) {
                    exclude("org.jetbrains.kotlin", "kotlin-stdlib")
                }
                implementation(kotlinx("coroutines-io", Versions.Kotlin.coroutinesIo))
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
                }
            }

            val androidTest by getting {
                dependencies {
                    implementation(kotlin("test", Versions.Kotlin.compiler))
                    implementation(kotlin("test-junit", Versions.Kotlin.compiler))
                    implementation(kotlin("test-annotations-common"))
                    implementation(kotlin("test-common"))
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
                implementation("org.bouncycastle:bcprov-jdk15on:1.64")
                implementation(kotlinx("io-jvm", Versions.Kotlin.io)) {
                    exclude("org.jetbrains.kotlin", "kotlin-stdlib")
                }
                //    api(kotlinx("coroutines-debug", Versions.Kotlin.coroutines))
            }
        }

        val jvmTest by getting {
            dependencies {
                dependsOn(commonTest)
                implementation(kotlin("test", Versions.Kotlin.compiler))
                implementation(kotlin("test-junit", Versions.Kotlin.compiler))
                implementation("org.pcap4j:pcap4j-distribution:1.8.2")

                runtimeOnly(files("build/classes/kotlin/jvm/main")) // classpath is not properly set by IDE
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}

apply(from = rootProject.file("gradle/publish.gradle"))


tasks.withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
    doFirst {
        publishing.publications
            .filterIsInstance<MavenPublication>()
            .forEach { publication ->
                val moduleFile = buildDir.resolve("publications/${publication.name}/module.json")
                if (moduleFile.exists()) {
                    publication.artifact(object :
                        org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact(moduleFile) {
                        override fun getDefaultExtension() = "module"
                    })
                }
            }
    }
}