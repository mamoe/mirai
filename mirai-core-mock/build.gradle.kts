/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.utils.addToStdlib.cast

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    id("me.him188.kotlin-jvm-blocking-bridge")
}

version = Versions.project
description = "Mirai core mock testing framework"


dependencies {
    api(project(":mirai-core"))
    api(project(":mirai-core-api"))
    api(project(":mirai-core-utils"))
    api(`ktor-server-core`)
    api(`ktor-server-netty`)
    api(`java-in-memory-file-system`)

    implementation(`kotlinx-serialization-protobuf-jvm`)
    implementation(`kotlinx-atomicfu-jvm`)
    implementation(`netty-all`)
    implementation(`log4j-api`)
    implementation(bouncycastle)
}

configurePublishing("mirai-core-mock")
tasks.named("shadowJar") { enabled = false }
