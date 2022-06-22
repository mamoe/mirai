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
    kotlin("plugin.serialization")
    id("java")
}

version = "0.0.0"

kotlin {
    explicitApiWarning()
}

dependencies {
    api(project(":mirai-console.integration-test"))
    api(parent!!.project("module-modb"))
    api(parent!!.project("module-private-issue2108"))
}

tasks.getByName("jar", Jar::class) {
    archiveBaseName.set("plugin-dep-dependon-dep-issue-2054-second-plugin")
}
