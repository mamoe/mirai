/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
import shadow.relocateCompileOnly
import shadow.relocateImplementation

plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    kotlin("plugin.serialization")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")
//    id("me.him188.maven-central-publish")
    `maven-publish`
}

description = "Mirai Protocol implementation for QQ Android"

kotlin {
    explicitApi()
    apply(plugin = "explicit-api")

    configureJvmTargetsHierarchical("net.mamoe.mirai.internal")
    configureNativeTargetsHierarchical(project)
    configureNativeTargetBinaries(project) // register native binaries for mirai-core only

    optInForAllSourceSets("net.mamoe.mirai.utils.MiraiExperimentalApi")
    optInForAllSourceSets("net.mamoe.mirai.utils.MiraiInternalApi")
    optInForAllSourceSets("net.mamoe.mirai.LowLevelApi")
    optInForAllSourceSets("kotlinx.serialization.ExperimentalSerializationApi")

    sourceSets.apply {

        val commonMain by getting {
            dependencies {
                api(project(":mirai-core-api"))
                api(`kotlinx-serialization-core`)
                api(`kotlinx-serialization-json`)
                api(`kotlinx-coroutines-core`)

                implementation(`kt-bignum`)
                implementation(project(":mirai-core-utils"))
                implementation(`kotlinx-serialization-protobuf`)
                implementation(`kotlinx-atomicfu`)

                // runtime from mirai-core-utils
                relocateCompileOnly(`ktor-io_relocated`)

//                relocateImplementation(`ktor-http_relocated`)
//                relocateImplementation(`ktor-serialization_relocated`)
//                relocateImplementation(`ktor-websocket-serialization_relocated`)
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
                implementation(`log4j-api`)
                implementation(`netty-handler`)
                api(`kotlinx-coroutines-jdk8`) // use -jvm modules for this magic target 'jvmBase'
            }
        }

        findByName("jvmBaseTest")?.apply {
            dependencies {
                implementation(`kotlinx-coroutines-debug`)
            }
        }

        findByName("androidMain")?.apply {
            dependencies {
                if (rootProject.property("mirai.android.target.api.level")!!.toString().toInt() < 23) {
                    // Ship with BC if we are targeting 23 or lower where AndroidKeyStore is not stable enough.
                    // For more info, read `net.mamoe.mirai.internal.utils.crypto.EcdhAndroidKt.create` in `androidMain`.
                    implementation(bouncycastle)
                }
            }
        }

        // For Android with JDK
        findByName("androidTest")?.apply {
            dependencies {
                implementation(bouncycastle)
            }
        }
        // For Android with SDK
        findByName("androidUnitTest")?.apply {
            dependencies {
                implementation(bouncycastle)
            }
        }

        findByName("jvmMain")?.apply {
            dependencies {
                implementation(bouncycastle)
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


        // Kt bignum
        findByName("jvmBaseMain")?.apply {
            dependencies {
                relocateImplementation(`kt-bignum_relocated`)
            }
        }


        // Ktor
        findByName("commonMain")?.apply {
            dependencies {
                compileOnly(`ktor-io`)
                implementation(`ktor-client-core`)
            }
        }
        findByName("jvmBaseMain")?.apply {
            // relocate for JVM like modules
            dependencies {
                relocateCompileOnly(`ktor-io_relocated`) // runtime from mirai-core-utils
                relocateImplementation(`ktor-client-core_relocated`)
            }
        }
        configure(NATIVE_TARGETS.map { getByName(it + "Main") }
                + NATIVE_TARGETS.map { getByName(it + "Test") }) {
            // no relocation in native, include binaries
            dependencies {
                api(`ktor-io`) {
                    exclude(ExcludeProperties.`slf4j-api`)
                }
            }
        }
        findByName("jvmBaseMain")?.apply {
            dependencies {
                relocateImplementation(`ktor-client-okhttp_relocated`)
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


        // Linkage
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

        disableCrossCompile()
//        val unixMain by getting {
//            dependencies {
//                implementation(`ktor-client-cio`)
//            }
//        }
    }
}

atomicfu {
    transformJvm = false
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
    tasks.getByName("androidBaseTest").dependsOn("checkAndroidApiLevel")
}

configureMppPublishing()
configureBinaryValidators(setOf("jvm", "android").filterTargets())

//mavenCentralPublish {
//    artifactId = "mirai-core"
//    githubProject("mamoe", "mirai")
//    developer("Mamoe Technologies", email = "support@mamoe.net", url = "https://github.com/mamoe")
//    licenseFromGitHubProject("AGPLv3", "dev")
//    publishPlatformArtifactsInRootModule = "jvm"
//}
