/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform")
    // id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")
    `maven-publish`
}

description = "mirai-core utilities additions"

kotlin {
    explicitApi()

    if (isAndroidSDKAvailable) {
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mirai-core-utils"))
            }
        }

        val commonTest by getting {
            dependencies {
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependencies {
                    compileOnly(`android-runtime`)
                    implementation(`zxing-core`)
                }
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(`zxing-javase`)
            }
        }

        val jvmTest by getting {
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }
    }
}

configureMppPublishing()

