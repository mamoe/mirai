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

plugins {
    kotlin("multiplatform")
    // id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")
    `maven-publish`
}

description = "Mirai Protocol implementation for QQ Android"

kotlin {
    explicitApi()

    configureHMPPJvm()

    sourceSets.apply {

        val commonMain by getting {
            dependencies {
                api(project(":mirai-core-api"))
                api(`kotlinx-serialization-core`)
                api(`kotlinx-serialization-json`)
                api(`kotlinx-coroutines-core`)

                implementation(project(":mirai-core-utils"))
                implementation(`kotlinx-serialization-protobuf`)
                implementation(`kotlinx-atomicfu`)
                implementation(`ktor-io`)
                implementation(`ktor-client-core`)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("script-runtime"))
                api(yamlkt)
            }
        }

        val jvmBaseMain by getting {
            dependencies {
                implementation(bouncycastle)
                implementation(`log4j-api`)
                implementation(`netty-all`)
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
                //  implementation("net.mamoe:mirai-login-solver-selenium:1.0-dev-14")
            }
        }

        val nativeMain by getting {
            dependencies {
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