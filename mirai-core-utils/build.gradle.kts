/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
    `maven-publish`
    id("com.jfrog.bintray")
}

description = "mirai-core ultilities"

val isAndroidSDKAvailable: Boolean by project

afterEvaluate {
    tasks.getByName("compileKotlinCommon").enabled = false
    tasks.getByName("compileTestKotlinCommon").enabled = false

    tasks.getByName("compileCommonMainKotlinMetadata").enabled = false
    tasks.getByName("compileKotlinMetadata").enabled = false
}

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

    jvm("common") {
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
    }

    jvm("jvm")

//    jvm("android") {
//        attributes.attribute(Attribute.of("mirai.target.platform", String::class.java), "android")
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("serialization"))
                api(kotlin("reflect"))

                api1(`kotlinx-serialization-core`)
                api1(`kotlinx-serialization-json`)
                implementation1(`kotlinx-serialization-protobuf`)
                api1(`kotlinx-io-jvm`)
                api1(`kotlinx-coroutines-io-jvm`)
                api(`kotlinx-coroutines-core`)

                implementation1(`kotlinx-atomicfu`)

                api1(`ktor-client-core`)
                api1(`ktor-network`)
            }
        }

        if (isAndroidSDKAvailable) {
            androidMain {
                dependencies {
                    api1(`ktor-client-android`)
                }
            }
        }

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
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