/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import BinaryCompatibilityConfigurator.configureBinaryValidators


plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    explicitApi()

    configureJvmTargetsHierarchical()
    configureNativeTargetsHierarchical(project)

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }

        commonTest {
            dependencies {
                implementation(`kotlinx-coroutines-core`)
                implementation(`kotlin-test`)
                implementation(kotest)
            }
        }
    }
}

//configureMppPublishing()
configureBinaryValidators(setOf("jvm", "android").filterTargets())