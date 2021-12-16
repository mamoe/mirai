/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnusedImport")

import java.util.Base64

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
}

version = Versions.console
description = "Mirai Console Backend Real-Time Testing Unit"

kotlin {
    explicitApiWarning()
}

dependencies {
    api(project(":mirai-core-api"))
    api(project(":mirai-core-utils"))
    api(project(":mirai-console-compiler-annotations"))
    api(project(":mirai-console"))
    api(project(":mirai-console-terminal"))

    api(`kotlin-stdlib-jdk8`)
    api(`kotlinx-atomicfu-jvm`)
    api(`kotlinx-coroutines-core-jvm`)
    api(`kotlinx-serialization-core-jvm`)
    api(`kotlinx-serialization-json-jvm`)
    api(`kotlin-reflect`)
    api(`kotlin-test-junit5`)


    api(`yamlkt-jvm`)
    api(`jetbrains-annotations`)
    api(`caller-finder`)
    api(`kotlinx-coroutines-jdk8`)


    val asmVersion = Versions.asm
    fun asm(module: String) = "org.ow2.asm:asm-$module:$asmVersion"

    api(asm("tree"))
    api(asm("util"))
    api(asm("commons"))

}

tasks.named<Test>("test").configure {
    val test0 = this
    doFirst {
        // For IDEA Debugging
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        val extArgs = test0.jvmArgs!!.asSequence().map { extArg ->
            Base64.getEncoder().encodeToString(extArg.toByteArray())
        }.joinToString(",")
        test0.jvmArgs = mutableListOf()
        test0.environment("IT_ARGS", extArgs)
    }
}
