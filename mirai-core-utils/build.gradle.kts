/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UNUSED_VARIABLE")

import shadow.relocateImplementation

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("kotlinx-atomicfu")
    id("me.him188.kotlin-jvm-blocking-bridge")
//    id("me.him188.maven-central-publish")
    `maven-publish`
}

description = "mirai-core utilities"

kotlin {
    explicitApi()
    apply(plugin = "explicit-api")

    configureJvmTargetsHierarchical("net.mamoe.mirai.utils")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("reflect"))
                api(`kotlinx-serialization-core`)
                api(`kotlinx-serialization-json`)
                api(`kotlinx-coroutines-core`)

                implementation(`kotlinx-serialization-protobuf`)
                relocateImplementation(`ktor-io_relocated`)
            }
        }

        val commonTest by getting {
            dependencies {
                api(yamlkt)
                implementation(`kotlinx-coroutines-test`)
                api(`junit-jupiter-api`)
            }
        }

        findByName("jvmBaseMain")?.apply {
            dependencies {
                implementation(`jetbrains-annotations`)
            }
        }

        findByName("androidMain")?.apply {
            dependencies {
                implementation(`androidx-annotation`)
            }
        }

        findByName("jvmMain")?.apply {

        }

        findByName("jvmTest")?.apply {
            dependencies {
                implementation(`kotlinx-coroutines-debug`)
                runtimeOnly(files("build/classes/kotlin/jvm/test")) // classpath is not properly set by IDE
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
    tasks.findByName("androidTest")?.dependsOn("checkAndroidApiLevel")
}

configureMppPublishing()

//mavenCentralPublish {
//    artifactId = "mirai-core-utils"
//    githubProject("mamoe", "mirai")
//    developer("Mamoe Technologies", email = "support@mamoe.net", url = "https://github.com/mamoe")
//    licenseFromGitHubProject("AGPLv3", "dev")
//    publishPlatformArtifactsInRootModule = "jvm"
//}