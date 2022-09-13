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
    id("java")
    `maven-publish`
}

dependencies {
    implementation("org.jline:jline:3.21.0")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    implementation(project(":mirai-console-frontend-base"))
    compileAndTestRuntime(project(":mirai-core-utils"))

    compileAndTestRuntime(project(":mirai-console"))
    compileAndTestRuntime(project(":mirai-core-api"))
    compileAndTestRuntime(project(":mirai-core-utils"))
    compileAndTestRuntime(kotlin("stdlib-jdk8", Versions.kotlinStdlib)) // must specify `compileOnly` explicitly

    testApi(project(":mirai-core"))
    testApi(project(":mirai-console"))
}

version = Versions.consoleTerminal

description = "Console Terminal CLI frontend for mirai"

configurePublishing("mirai-console-terminal")


val copyResources by tasks.creating(Copy::class){
    dependsOn(":mirai-core:jvmProcessResources")
    dependsOn(":mirai-core-api:jvmProcessResources")
    from(project(":mirai-core").buildDir.resolve("processedResources/jvm"))
    from(project(":mirai-core-api").buildDir.resolve("processedResources/jvm"))
    into(buildDir.resolve("resources"))
}

tasks.getByName("classes").dependsOn(copyResources)
// endregion