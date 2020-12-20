/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
}

version = Versions.project
description = "Mirai core shadowed"

dependencies {
    api(project(":mirai-core"))
    api(project(":mirai-core-api"))
    api(project(":mirai-core-utils"))
}

configurePublishing("mirai-core-all")