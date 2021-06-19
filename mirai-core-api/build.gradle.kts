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
    id("net.mamoe.kotlin-jvm-blocking-bridge")

    `maven-publish`
}

description = "Mirai API module"

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

//    jvm("android") {
//        attributes.attribute(Attribute.of("mirai.target.platform", String::class.java), "android")
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mirai-core-utils"))
                api(kotlin("reflect"))

                api(`kotlinx-serialization-core`)
                api(`kotlinx-serialization-json`)
                implementation(`kotlinx-serialization-protobuf`)
                api(`kotlinx-coroutines-jdk8`)
                implementation(`jetbrains-annotations`)
                // api(`kotlinx-coroutines-jdk8`)

                api(`ktor-client-okhttp`)
                api(`ktor-client-core`)
                api(`ktor-network`)

                compileOnly(`log4j-api`)
                compileOnly(slf4j)


                // they use Kotlin 1.3 so we need to ignore transitive dependencies
                api1(`kotlinx-io-jvm`)
                api1(`kotlinx-coroutines-io-jvm`)
                implementation1(`kotlinx-atomicfu`)
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                dependsOn(commonMain)
                dependencies {
                    compileOnly(`android-runtime`)
                    api1(`ktor-client-android`)
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

afterEvaluate {
    project(":binary-compatibility-validator").tasks["apiBuild"].dependsOn(project(":mirai-core-api").tasks["build"])
    project(":binary-compatibility-validator-android").tasks["apiBuild"].dependsOn(project(":mirai-core-api").tasks["build"])
}