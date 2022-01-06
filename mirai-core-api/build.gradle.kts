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
    kotlin("plugin.serialization")

    //id("kotlinx-atomicfu")
    id("signing")
    id("me.him188.kotlin-jvm-blocking-bridge")

    `maven-publish`
}

description = "Mirai API module"

kotlin {
    explicitApi()

    if (isAndroidSDKAvailable) {
        jvm("android") {
            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
        }
    } else {
        printAndroidNotInstalled()
    }

//    jvm("jvmCommon") {
//        attributes.attribute(MIRAI_PLATFORM, "jvmCommon")
//    }

    jvm("jvm")


    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("reflect"))
                api(`kotlinx-serialization-core-jvm`)
                api(`kotlinx-serialization-json-jvm`)
                api(`kotlinx-coroutines-core-jvm`) // don't remove it, otherwise IDE will complain
                api(`kotlinx-coroutines-jdk8`)
                api(`ktor-client-okhttp`)

                implementation(project(":mirai-core-utils"))
                implementation(project(":mirai-console-compiler-annotations"))
                implementation(`kotlinx-serialization-protobuf-jvm`)
                implementation(`jetbrains-annotations`)
                implementation(`log4j-api`)
                implementation(`kotlinx-atomicfu-jvm`)
                implementationKotlinxIoJvm()

                compileOnly(`slf4j-api`)
            }
        }

        val commonTest by getting {
            dependencies {
                runtimeOnly(`log4j-core`)
            }
        }

        val jvmCommonMain by creating {
            dependsOn(commonMain)
        }

        val jvmCommonTest by creating {
            dependsOn(commonTest)
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependsOn(jvmCommonMain)
                dependencies {
                    compileOnly(`android-runtime`)
//                    api(`ktor-client-android`)
                }
            }
            val androidTest by getting {
                dependsOn(jvmCommonTest)
            }
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
        }

        val jvmTest by getting {
            dependsOn(jvmCommonTest)
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
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

afterEvaluate {
    project(":binary-compatibility-validator").tasks["apiBuild"].dependsOn(project(":mirai-core-api").tasks["build"])
    project(":binary-compatibility-validator-android").tasks["apiBuild"].dependsOn(project(":mirai-core-api").tasks["build"])
}