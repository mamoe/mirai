/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

/*
* Copyright 2019-2021 Mamoe Technologies and contributors.
*
*  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
*  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
*
*  https://github.com/mamoe/mirai/blob/master/LICENSE
*/

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("kotlinx-atomicfu")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
}

description = "Mirai API binary compatibility validator"

tasks.withType(kotlinx.validation.KotlinApiBuildTask::class) {
    inputClassesDirs =
        files(inputClassesDirs.files, project(":mirai-core-api").buildDir.resolve("classes/kotlin/jvm/main"))
}

// tasks["apiDump"].dependsOn(project(":mirai-core-api").tasks["build"])
// this dependency is set in mirai-core-api since binary validator is configured before mirai-core-api