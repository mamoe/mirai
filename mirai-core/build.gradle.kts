/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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
    // id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
    `maven-publish`
}

description = "Mirai Protocol implementation for QQ Android"

afterEvaluate {
    tasks.getByName("compileKotlinCommon").enabled = false
    tasks.getByName("compileTestKotlinCommon").enabled = false

    tasks.getByName("compileCommonMainKotlinMetadata").enabled = false
    tasks.getByName("compileKotlinMetadata").enabled = false
}

kotlin {
    explicitApi()

    if (isAndroidSDKAvailable) {
//        apply(from = rootProject.file("gradle/android.gradle"))
//        android("android") {
//            publishAllLibraryVariants()
//        }
        jvm("android") {
            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
            //   publishAllLibraryVariants()
        }
    } else {
        printAndroidNotInstalled()
    }

    jvm("common") {
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
    }

    jvm("jvm")

    /*
    jvm("android") {
        attributes.attribute(ATTRIBUTE_MIRAI_TARGET_PLATFORM, "android")
    }*/

    sourceSets.apply {

        val commonMain by getting {
            dependencies {
                api(project(":mirai-core-api"))
                implementation(project(":mirai-core-utils"))
                api1(`kotlinx-serialization-core`)
                api1(`kotlinx-serialization-json`)
                implementation1(`kotlinx-serialization-protobuf`)

                api1(`kotlinx-atomicfu`)
                api1(`kotlinx-coroutines-core`)

                api1(`kotlinx-io-jvm`)
                implementation1(`kotlinx-coroutines-io`)
                implementation(`netty-all`)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("script-runtime"))
                runtimeOnly(`slf4j-simple`)
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependsOn(commonMain)
                dependencies {
                    compileOnly(`android-runtime`)
                }
            }
            val androidTest by getting {
                dependencies {
                    implementation(kotlin("test", Versions.kotlinCompiler))
                    implementation(kotlin("test-junit5", Versions.kotlinCompiler))
                    implementation(kotlin("test-annotations-common"))
                    implementation(kotlin("test-common"))
                    implementation("org.bouncycastle:bcprov-jdk15on:1.64")
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.bouncycastle:bcprov-jdk15on:1.64")
                // api(kotlinx("coroutines-debug", Versions.coroutines))
            }
        }

        val jvmTest by getting {
            dependencies {
                api1(`kotlinx-coroutines-debug`)
                //  implementation("net.mamoe:mirai-login-solver-selenium:1.0-dev-14")
            }
        }
    }
}

if (isAndroidSDKAvailable) {
    tasks.register("checkAndroidApiLevel") {
        doFirst {
            analyzes.AndroidApiLevelCheck.check(
                buildDir.resolve("classes/kotlin/android/main"),
                project.property("mirai.android.target.api.level")!!.toString().toInt(),
                project
            )
        }
        group = "verification"
        this.mustRunAfter("androidMainClasses")
    }
    tasks.getByName("androidTest").dependsOn("checkAndroidApiLevel")
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

configureMppPublishing()