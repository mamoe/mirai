/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import shadow.registerRegularShadowTaskForJvmProject
import shadow.shadowImplementation

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
}

val shadow: Configuration = project.configurations.create("shadow")

kotlin {
    optInForTestSourceSets("net.mamoe.mirai.console.util.ConsoleExperimentalApi")
    optInForTestSourceSets("net.mamoe.mirai.console.ConsoleFrontEndImplementation")
}

dependencies {
    api(project(":mirai-core-api"))
    api(project(":mirai-console"))
    implementation(project(":mirai-core-utils"))

    shadowImplementation(jline)
    shadowImplementation(jansi)
    shadowImplementation(project(":mirai-console-frontend-base"))
    implementation(`kotlin-jvm-blocking-bridge`)

    testImplementation(project(":mirai-core"))
}

version = Versions.consoleTerminal

description = "Console Terminal CLI frontend for mirai"

configurePublishing("mirai-console-terminal", addShadowJar = false)
val shadowJar = registerRegularShadowTaskForJvmProject(listOf(shadow))
publishing {
    publications.getByName("mavenJava", MavenPublication::class) {
        artifacts.artifact(shadowJar)
    }
}