@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("signing")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
    `maven-publish`
    id("com.jfrog.bintray") version Versions.Publishing.bintray
}

description = "Mirai API module"

val isAndroidSDKAvailable: Boolean by project

kotlin {
    explicitApi()

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

    jvm {
        // withJava() // https://youtrack.jetbrains.com/issue/KT-39991
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
        }

        val commonMain by getting {
            dependencies {
                api(kotlin("serialization"))
                api(kotlin("reflect"))

                api1(kotlinx("serialization-core", Versions.Kotlin.serialization))
                implementation1(kotlinx("serialization-protobuf", Versions.Kotlin.serialization))
                api1(kotlinx("io", Versions.Kotlin.io))
                api1(kotlinx("coroutines-io", Versions.Kotlin.coroutinesIo))
                api(kotlinx("coroutines-core", Versions.Kotlin.coroutines))

                implementation1("org.jetbrains.kotlinx:atomicfu:${Versions.Kotlin.atomicFU}")

                api1(ktor("client-cio"))
                api1(ktor("client-core"))
                api1(ktor("network"))
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

                    api1(kotlinx("io-jvm", Versions.Kotlin.io))
                    api1(kotlinx("coroutines-io-jvm", Versions.Kotlin.coroutinesIo))

                    api1(ktor("client-android", Versions.Kotlin.ktor))
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
                api(kotlin("reflect"))
                compileOnly("org.apache.logging.log4j:log4j-api:" + Versions.Logging.log4j)
                compileOnly("org.slf4j:slf4j-api:" + Versions.Logging.slf4j)

                api1(ktor("client-core-jvm", Versions.Kotlin.ktor))
                api1(kotlinx("io-jvm", Versions.Kotlin.io))
                api1(kotlinx("coroutines-io-jvm", Versions.Kotlin.coroutinesIo))
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

fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.implementation1(dependencyNotation: String) =
    implementation(dependencyNotation) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-common")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-metadata")
    }

fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.api1(dependencyNotation: String) =
    api(dependencyNotation) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-common")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-metadata")
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