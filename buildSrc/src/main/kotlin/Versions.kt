/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("ObjectPropertyName", "ObjectPropertyName", "unused")

import org.gradle.api.attributes.Attribute

/*
* Copyright 2019-2020 Mamoe Technologies and contributors.
*
* 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
* Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
*
* https://github.com/mamoe/mirai/blob/master/LICENSE
*/

object Versions {
    const val project = "2.0-M2-2"

    const val kotlinCompiler = "1.4.21"
    const val kotlinStdlib = "1.4.21"
    const val coroutines = "1.4.1"
    const val atomicFU = "0.14.4"
    const val serialization = "1.0.1"
    const val ktor = "1.5.0"

    const val binaryValidator = "0.2.3"

    const val io = "0.1.16"
    const val coroutinesIo = "0.1.16"
    const val dokka = "0.10.1"

    const val blockingBridge = "1.5.0"

    const val androidGradlePlugin = "3.5.3"

    const val bintray = "1.8.5"

    const val slf4j = "1.7.30"
    const val log4j = "2.13.3"
}

@Suppress("unused")
fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

@Suppress("unused")
fun ktor(id: String, version: String = Versions.ktor) = "io.ktor:ktor-$id:$version"


val `kotlinx-coroutines-core` = kotlinx("coroutines-core", Versions.coroutines)
val `kotlinx-serialization-core` = kotlinx("serialization-core", Versions.serialization)
val `kotlinx-serialization-json` = kotlinx("serialization-json", Versions.serialization)
val `kotlinx-serialization-protobuf` = kotlinx("serialization-protobuf", Versions.serialization)
const val `kotlinx-atomicfu` = "org.jetbrains.kotlinx:atomicfu:${Versions.atomicFU}"
val `kotlinx-io` = kotlinx("io", Versions.io)
val `kotlinx-io-jvm` = kotlinx("io-jvm", Versions.io)
val `kotlinx-coroutines-io` = kotlinx("coroutines-io", Versions.coroutinesIo)
val `kotlinx-coroutines-io-jvm` = kotlinx("coroutines-io-jvm", Versions.coroutinesIo)

val `ktor-serialization` = ktor("serialization", Versions.ktor)

val `ktor-client-core` = ktor("client-core", Versions.ktor)
val `ktor-client-cio` = ktor("client-cio", Versions.ktor)
val `ktor-client-okhttp` = ktor("client-okhttp", Versions.ktor)
val `ktor-client-android` = ktor("client-android", Versions.ktor)
val `ktor-network` = ktor("network", Versions.ktor)
val `ktor-client-serialization-jvm` = ktor("client-serialization-jvm", Versions.ktor)

const val slf4j = "org.slf4j:slf4j-api:" + Versions.slf4j
const val `log4j-api` = "org.apache.logging.log4j:log4j-api:" + Versions.log4j

val ATTRIBUTE_MIRAI_TARGET_PLATFORM: Attribute<String> = Attribute.of("mirai.target.platform", String::class.java)