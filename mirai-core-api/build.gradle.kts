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

    id("kotlinx-atomicfu")
    id("signing")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")
//    id("me.him188.maven-central-publish")

    `maven-publish`
}

description = "Mirai API module"

kotlin {
    explicitApi()
    configureJvmTargetsHierarchical()

    configureNativeTargetsHierarchical(project)


    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("reflect"))
                api(`kotlinx-serialization-core`)
                api(`kotlinx-serialization-json`)
                api(`kotlinx-coroutines-core`) // don't remove it, otherwise IDE will complain

                implementation(project(":mirai-core-utils"))
                implementation(project(":mirai-console-compiler-annotations"))
                implementation(`kotlinx-serialization-protobuf`)
                relocateCompileOnly(`ktor-io`) // runtime from mirai-core-utils
            }
        }

        commonTest {
            dependencies {
                runtimeOnly(`log4j-core`)
                implementation(`kotlinx-coroutines-test`)
            }
        }

        findByName("jvmBaseMain")?.apply {
            dependencies {
                implementation(`jetbrains-annotations`)
                implementation(`log4j-api`)
                compileOnly(`slf4j-api`)
            }
        }

        findByName("androidMain")?.apply {
            dependsOn(commonMain)
            dependencies {
                compileOnly(`android-runtime`)
            }
        }

        findByName("jvmMain")?.apply {

        }

        findByName("jvmTest")?.apply {
            dependencies {
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
            }
        }

        findByName("nativeMain")?.apply {
            dependencies {
            }
        }
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
relocateKtorForCore(false)

//mavenCentralPublish {
//    artifactId = "mirai-core-api"
//    githubProject("mamoe", "mirai")
//    developer("Mamoe Technologies", email = "support@mamoe.net", url = "https://github.com/mamoe")
//    licenseFromGitHubProject("AGPLv3", "dev")
//    publishPlatformArtifactsInRootModule = "jvm"
//}