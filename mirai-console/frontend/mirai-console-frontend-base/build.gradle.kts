/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import BinaryCompatibilityConfigurator.configureBinaryValidator

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
}

kotlin {
    explicitApiWarning()
}

dependencies {
    compileAndTestRuntime(project(":mirai-core-utils"))

    compileAndTestRuntime(project(":mirai-console"))
    compileAndTestRuntime(project(":mirai-core-api"))
    compileAndTestRuntime(project(":mirai-core-utils"))
    compileAndTestRuntime(`kotlin-jvm-blocking-bridge`)
    compileAndTestRuntime(`kotlin-stdlib-jdk8`)
}

version = Versions.consoleTerminal

description = "Console frontend abstract"

configurePublishing("mirai-console-frontend-base")
configureBinaryValidator(null)

