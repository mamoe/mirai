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
    kotlin("plugin.serialization")

    //id("kotlinx-atomicfu")
    id("signing")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")

    `maven-publish`
}

description = "Mirai API module"

kotlin {
    explicitApi()
    configureHMPP()

    configureNativeTargetsHierarchical(project)


    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("reflect"))
                api(`kotlinx-serialization-core`)
                api(`kotlinx-serialization-json`)
                api(`kotlinx-coroutines-core`) // don't remove it, otherwise IDE will complain
                implementation(`ktor-client-core`)

                implementation(project(":mirai-core-utils"))
                implementation(project(":mirai-console-compiler-annotations"))
                implementation(`kotlinx-serialization-protobuf`)
                implementation(`kotlinx-atomicfu`)
                implementation(`ktor-io`)
            }
        }

        commonTest {
            dependencies {
                runtimeOnly(`log4j-core`)
            }
        }

        val jvmBaseMain by getting {
            dependencies {
                api(`ktor-client-okhttp`)
                api(`kotlinx-coroutines-jdk8`)
                implementation(`jetbrains-annotations`)
                implementation(`log4j-api`)
                compileOnly(`slf4j-api`)
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependsOn(commonMain)
                dependencies {
                    compileOnly(`android-runtime`)
//                    api(`ktor-client-android`)
                }
            }
        }

        val jvmMain by getting {

        }

        val jvmTest by getting {
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
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