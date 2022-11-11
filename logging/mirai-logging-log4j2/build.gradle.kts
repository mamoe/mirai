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

version = Versions.core
description = "Mirai Log4J Adapter"

kotlin {
    explicitApi()
}

dependencies {
    api(project(":mirai-core-api"))
    api(`log4j-api`)
    api(`log4j-core`)
    api(`log4j-slf4j2-impl`)


    testImplementation(`slf4j-api`)
    testImplementation(project(":mirai-core"))
    testImplementation(project(":mirai-core-utils"))
    testImplementation(`ktor-client-okhttp`)
}

configurePublishing("mirai-logging-log4j2")