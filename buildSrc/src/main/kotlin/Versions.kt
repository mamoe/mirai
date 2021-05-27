/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName", "unused")

import org.gradle.api.attributes.Attribute

object Versions {
    const val core = "2.6.4-dev-kt15"  // WARNING: This might not be up-to-date since included in mirai build.
    const val console = "2.6.4-dev-kt15"
    const val consoleGraphical = "0.0.7"
    const val consoleTerminal = console

    const val kotlinCompiler = "1.5.10"
    const val kotlinStdlib = "1.5.10"

    const val kotlinIntellijPlugin = "211-1.4.32-release-IJ6693.72" // keep to newest as kotlinCompiler
    const val intellij = "2021.1" // don't update easily unless you want your disk space -= 500MB


    const val coroutines = "1.5.0"
    const val atomicFU = "0.16.1"
    const val serialization = "1.1.0"
    const val ktor = "1.5.4"

    const val androidGradle = "3.6.2"

    const val blockingBridge = "1.10.3"

    const val junit = "5.4.2"

    @Suppress("SpellCheckingInspection")
    const val yamlkt = "0.9.0"

    const val intellijGradlePlugin = "0.7.2"
}

const val `kotlin-compiler` = "org.jetbrains.kotlin:kotlin-compiler:${Versions.kotlinCompiler}"

const val `kotlin-stdlib` = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlinStdlib}"
const val `kotlin-stdlib-jdk8` = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinStdlib}"
const val `kotlin-reflect` = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinStdlib}"
const val `kotlin-test` = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlinStdlib}"
const val `kotlin-test-junit5` = "org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlinStdlib}"

const val `kotlinx-coroutines-core` = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
const val `kotlinx-coroutines-jdk8` = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}"
const val `kotlinx-coroutines-swing` = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.coroutines}"

const val `kotlinx-serialization-core` = "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}"
const val `kotlinx-serialization-json` = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}"
const val `kotlinx-serialization-protobuf` = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.serialization}"

const val `kotlinx-atomicfu` = "org.jetbrains.kotlinx:atomicfu:${Versions.atomicFU}"

const val yamlkt = "net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}"

const val `jetbrains-annotations` = "org.jetbrains:annotations:19.0.0"


const val `caller-finder` = "io.github.karlatemp:caller:1.1.1"


val ATTRIBUTE_MIRAI_TARGET_PLATFORM: Attribute<String> = Attribute.of("mirai.target.platform", String::class.java)