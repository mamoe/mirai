/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UNUSED_VARIABLE")

import BinaryCompatibilityConfigurator.configureBinaryValidators
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform")
    // id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")
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
        jvm("android") {
            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
        }
    } else {
        printAndroidNotInstalled()
    }

    jvm("common") {
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
    }

    jvm("jvm")

    sourceSets.apply {

        val commonMain by getting {
            dependencies {
                api(project(":mirai-core-api"))
                api(`kotlinx-serialization-core-jvm`)
                api(`kotlinx-serialization-json-jvm`)
                api(`kotlinx-coroutines-core-jvm`)

                implementation(project(":mirai-core-utils"))
                implementation(`kotlinx-serialization-protobuf-jvm`)
                implementation(`kotlinx-atomicfu-jvm`)
                implementation(`netty-all`)
                implementation(`log4j-api`)
                implementation(bouncycastle)
                implementationKotlinxIoJvm()
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("script-runtime"))
                api(`yamlkt-jvm`)
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
                    //implementation("org.bouncycastle:bcprov-jdk15on:1.64")
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                //implementation("org.bouncycastle:bcprov-jdk15on:1.64")
                // api(kotlinx("coroutines-debug", Versions.coroutines))
            }
        }

        val jvmTest by getting {
            dependencies {
                api(`kotlinx-coroutines-debug`)
                api(project(":mirai-core-utils-barcode"))
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

configureMppPublishing()
configureBinaryValidators("jvm", "android")