@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("signing")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
    `maven-publish`
    id("com.jfrog.bintray")
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
        withJava()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("serialization"))
                api(kotlin("reflect"))

                api1(`kotlinx-serialization-core`)
                implementation1(`kotlinx-serialization-protobuf`)
                api1(`kotlinx-io`)
                api1(`kotlinx-coroutines-io`)
                api(`kotlinx-coroutines-core`)

                implementation1(`kotlinx-atomicfu`)

                api1(`ktor-client-cio`)
                api1(`ktor-client-core`)
                api1(`ktor-network`)
            }
        }

        if (isAndroidSDKAvailable) {
            androidMain {
                dependencies {
                    api(kotlin("reflect"))

                    api1(`kotlinx-io-jvm`)
                    api1(`kotlinx-coroutines-io-jvm`)

                    api1(`ktor-client-android`)
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("reflect"))
                compileOnly(`log4j-api`)
                compileOnly(slf4j)

                api1(`kotlinx-io-jvm`)
                api1(`kotlinx-coroutines-io-jvm`)
            }
        }

        val jvmTest by getting {
            dependencies {
                api("org.pcap4j:pcap4j-distribution:1.8.2")

                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}

val NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.androidMain: NamedDomainObjectProvider<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>
    get() = named<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>("androidMain")

val NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.androidTest: NamedDomainObjectProvider<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>
    get() = named<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>("androidTest")


val NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.jvmMain: NamedDomainObjectProvider<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>
    get() = named<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>("jvmMain")

val NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.jvmTest: NamedDomainObjectProvider<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>
    get() = named<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>("jvmTest")


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