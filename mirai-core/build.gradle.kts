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
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    // id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")
//    id("me.him188.maven-central-publish")
    `maven-publish`
}

description = "Mirai Protocol implementation for QQ Android"

kotlin {
    explicitApi()

    configureJvmTargetsHierarchical()
    configureNativeTargetsHierarchical(project)

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
                implementation(`kotlinx-coroutines-test`)
                api(yamlkt)
            }
        }

        findByName("jvmBaseMain")?.apply {
            dependencies {
                implementation(bouncycastle)
                implementation(`log4j-api`)
                implementation(`netty-all`)
                implementation(`ktor-client-okhttp`)
                api(`kotlinx-coroutines-jdk8`) // use -jvm modules for this magic target 'jvmBase'
            }
        }

        findByName("jvmBaseTest")?.apply {
            dependencies {
                implementation(`kotlinx-coroutines-debug`)
            }
        }

        findByName("androidMain")?.apply {
            dependsOn(commonMain)
            dependencies {
                compileOnly(`android-runtime`)
            }
        }
        findByName("androidTest")?.apply {
            dependencies {
                implementation(kotlin("test", Versions.kotlinCompiler))
                implementation(kotlin("test-junit5", Versions.kotlinCompiler))
                implementation(kotlin("test-annotations-common"))
                implementation(kotlin("test-common"))
                //implementation("org.bouncycastle:bcprov-jdk15on:1.64")
            }
        }

        findByName("jvmMain")?.apply {
            dependencies {
                //implementation("org.bouncycastle:bcprov-jdk15on:1.64")
                // api(kotlinx("coroutines-debug", Versions.coroutines))
            }
        }

        findByName("jvmTest")?.apply {
            dependencies {
                api(`kotlinx-coroutines-debug`)
                //  implementation("net.mamoe:mirai-login-solver-selenium:1.0-dev-14")
            }
        }

        findByName("nativeMain")?.apply {
            dependencies {
            }
        }

        NATIVE_TARGETS.forEach { targetName ->
            val defFile = projectDir.resolve("src/nativeMain/cinterop/OpenSSL.def")
            val target = targets.getByName(targetName) as KotlinNativeTarget
            target.compilations.getByName("main").cinterops.create("OpenSSL")
                .apply {
                    this.defFile = defFile
                    packageName("openssl")
                }

            configure(target.binaries.filterIsInstance<AbstractNativeLibrary>()) {
                export(project(":mirai-core-api"))
                export(project(":mirai-core-utils"))
            }
        }

        UNIX_LIKE_TARGETS.forEach { target ->
            (targets.getByName(target) as KotlinNativeTarget).compilations.getByName("main").cinterops.create("Socket")
                .apply {
                    defFile = projectDir.resolve("src/unixMain/cinterop/Socket.def")
                    packageName("sockets")
                }
        }

        WIN_TARGETS.forEach { target ->
            (targets.getByName(target) as KotlinNativeTarget).compilations.getByName("main").cinterops.create("Socket")
                .apply {
                    defFile = projectDir.resolve("src/mingwX64Main/cinterop/Socket.def")
                    packageName("sockets")
                }
        }

        configure(WIN_TARGETS.map { getByName(it + "Main") }) {
            dependencies {
                implementation(`ktor-client-curl`)
            }
        }

        configure(LINUX_TARGETS.map { getByName(it + "Main") }) {
            dependencies {
                implementation(`ktor-client-cio`)
            }
        }

        findByName("darwinMain")?.apply {
            dependencies {
                implementation(`ktor-client-darwin`)
            }
        }

        disableCrossCompile()
//        val unixMain by getting {
//            dependencies {
//                implementation(`ktor-client-cio`)
//            }
//        }
    }
}

afterEvaluate {
    val main = projectDir.resolve("src/nativeTest/kotlin/local/TestMain.kt")
    if (!main.exists()) {
        main.writeText(
            """
            /*
             * Copyright 2019-2022 Mamoe Technologies and contributors.
             *
             * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
             * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
             *
             * https://github.com/mamoe/mirai/blob/dev/LICENSE
             */

            package net.mamoe.mirai.internal.local

            fun main() {}
        """.trimIndent()
        )
    }
}

if (tasks.findByName("androidMainClasses") != null) {
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
configureBinaryValidators(setOf("jvm", "android").filterTargets())
configureRelocationForCore()

//mavenCentralPublish {
//    artifactId = "mirai-core"
//    githubProject("mamoe", "mirai")
//    developer("Mamoe Technologies", email = "support@mamoe.net", url = "https://github.com/mamoe")
//    licenseFromGitHubProject("AGPLv3", "dev")
//    publishPlatformArtifactsInRootModule = "jvm"
//}