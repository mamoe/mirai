/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    id("java")
    `maven-publish`
}

repositories {
    maven("http://maven.aliyun.com/nexus/content/groups/public/")
}

version = Versions.console
description = "Mirai Console compiler resolve"

dependencies {
    api(`jetbrains-annotations`)
    // api(`kotlinx-coroutines-jdk8`)
    api(project(":mirai-console-compiler-annotations"))

    compileOnly(`kotlin-compiler`)
    testRuntimeOnly(`kotlin-compiler`)
}

configurePublishing("mirai-console-compiler-common")