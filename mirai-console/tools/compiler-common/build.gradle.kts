/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    id("java")
    `maven-publish`
}

repositories {
    maven("https://maven.aliyun.com/repository/central")
}

version = Versions.console
description = "Mirai Console compiler resolve"

dependencies {
    api(`jetbrains-annotations`)
    // api(`kotlinx-coroutines-jdk8`)
    api(project(":mirai-console-compiler-annotations"))

    compileOnly(`kotlin-compiler_forIdea`)
    testRuntimeOnly(`kotlin-compiler_forIdea`)
}

configurePublishing("mirai-console-compiler-common")