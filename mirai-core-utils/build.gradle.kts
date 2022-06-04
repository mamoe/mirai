/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("kotlinx-atomicfu")
    id("me.him188.kotlin-jvm-blocking-bridge")
    `maven-publish`
}

description = "mirai-core utilities"

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
                api(`kotlinx-coroutines-core`)

                implementation(`kotlinx-atomicfu`)
                implementation(`kotlinx-serialization-protobuf`)
                implementation(`ktor-io`)
            }
        }

        val commonTest by getting {
            dependencies {
                api(yamlkt)
            }
        }

        val jvmBaseMain by getting {
            dependencies {
                implementation(`jetbrains-annotations`)
            }
        }

        if (isAndroidSDKAvailable) {
            val androidMain by getting {
                //
                dependencies {
                    compileOnly(`android-runtime`)
//                    api1(`ktor-client-android`)
                }
            }
        }

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }

        val nativeMain by getting {
            dependencies {
//                implementation("com.soywiz.korlibs.krypto:krypto:2.4.12") // ':mirai-core-utils:compileNativeMainKotlinMetadata' fails because compiler cannot find reference
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
