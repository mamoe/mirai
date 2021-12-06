/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiConsoleGradlePluginKt")

package net.mamoe.mirai.console.gradle

internal val IGNORED_DEPENDENCIES_IN_SHADOW = arrayOf(
    "org.jetbrains.kotlin:kotlin-stdlib",
    "org.jetbrains.kotlin:kotlin-stdlib-common",
    "org.jetbrains.kotlin:kotlin-stdlib-metadata",
    "org.jetbrains.kotlin:kotlin-stdlib-jvm",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk7",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",

    "org.jetbrains.kotlin:kotlin-reflect",
    "org.jetbrains.kotlin:kotlin-reflect-common",
    "org.jetbrains.kotlin:kotlin-reflect-metadata",
    "org.jetbrains.kotlin:kotlin-reflect-jvm",

    "org.jetbrains.kotlinx:kotlinx-serialization-core",
    "org.jetbrains.kotlinx:kotlinx-serialization-core-common",
    "org.jetbrains.kotlinx:kotlinx-serialization-core-metadata",
    "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm",

    "org.jetbrains.kotlinx:kotlinx-serialization-runtime",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-metadata",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-jvm",

    "org.jetbrains.kotlinx:kotlinx-serialization-protobuf",
    "org.jetbrains.kotlinx:kotlinx-serialization-protobuf-common",
    "org.jetbrains.kotlinx:kotlinx-serialization-protobuf-metadata",
    "org.jetbrains.kotlinx:kotlinx-serialization-protobuf-jvm",

    "org.jetbrains.kotlinx:kotlinx-coroutines-core",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-common",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-metadata",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm",

    "org.jetbrains.kotlinx:kotlinx-io",
    "org.jetbrains.kotlinx:kotlinx-io-common",
    "org.jetbrains.kotlinx:kotlinx-io-metadata",
    "org.jetbrains.kotlinx:kotlinx-io-jvm",

    "org.jetbrains.kotlinx:kotlinx-coroutines-io",
    "org.jetbrains.kotlinx:kotlinx-coroutines-io-common",
    "org.jetbrains.kotlinx:kotlinx-coroutines-io-metadata",
    "org.jetbrains.kotlinx:kotlinx-coroutines-io-jvm",

    "org.jetbrains.kotlinx:kotlinx-coroutines-core-jdk7",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-jdk8",

    "org.jetbrains.kotlinx:kotlinx-coroutines-jdk7",
    "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8",

    "org.jetbrains.kotlinx:atomicFu",
    "org.jetbrains.kotlinx:atomicFu-common",
    "org.jetbrains.kotlinx:atomicFu-metadata",
    "org.jetbrains.kotlinx:atomicFu-jvm",

    "org.jetbrains:annotations:19.0.0",

    "io.ktor:ktor-client",
    "io.ktor:ktor-client-common",
    "io.ktor:ktor-client-metadata",
    "io.ktor:ktor-client-jvm",

    "io.ktor:ktor-client-cio",
    "io.ktor:ktor-client-cio-common",
    "io.ktor:ktor-client-cio-metadata",
    "io.ktor:ktor-client-cio-jvm",

    "io.ktor:ktor-client-core",
    "io.ktor:ktor-client-core-common",
    "io.ktor:ktor-client-core-metadata",
    "io.ktor:ktor-client-core-jvm",

    "io.ktor:ktor-client-network",
    "io.ktor:ktor-client-network-common",
    "io.ktor:ktor-client-network-metadata",
    "io.ktor:ktor-client-network-jvm",

    "io.ktor:ktor-client-util",
    "io.ktor:ktor-client-util-common",
    "io.ktor:ktor-client-util-metadata",
    "io.ktor:ktor-client-util-jvm",

    "io.ktor:ktor-client-http",
    "io.ktor:ktor-client-http-common",
    "io.ktor:ktor-client-http-metadata",
    "io.ktor:ktor-client-http-jvm",

    "org.bouncyCastle:bcProv-jdk15on",

    "net.mamoe:mirai-core",
    "net.mamoe:mirai-core-metadata",
    "net.mamoe:mirai-core-common",
    "net.mamoe:mirai-core-jvm",
    "net.mamoe:mirai-core-android",
    "net.mamoe:mirai-core-jvmCommon",
    "net.mamoe:mirai-core-commonJvm",

    "net.mamoe:mirai-core-utils",
    "net.mamoe:mirai-core-utils-metadata",
    "net.mamoe:mirai-core-utils-common",
    "net.mamoe:mirai-core-utils-jvm",
    "net.mamoe:mirai-core-utils-android",
    "net.mamoe:mirai-core-utils-jvmCommon",
    "net.mamoe:mirai-core-utils-commonJvm",

    "net.mamoe:mirai-core-api",
    "net.mamoe:mirai-core-api-metadata",
    "net.mamoe:mirai-core-api-common",
    "net.mamoe:mirai-core-api-jvm",
    "net.mamoe:mirai-core-api-android",
    "net.mamoe:mirai-core-api-jvmCommon",
    "net.mamoe:mirai-core-api-commonJvm",

    "net.mamoe:mirai-core-qqAndroid",
    "net.mamoe:mirai-core-qqAndroid-metadata",
    "net.mamoe:mirai-core-qqAndroid-common",
    "net.mamoe:mirai-core-qqAndroid-jvm",

    "net.mamoe:mirai-console",
    "net.mamoe:mirai-console-api", // for future
    "net.mamoe:mirai-console-terminal",
    "net.mamoe:mirai-console-graphical",

    "net.mamoe.yamlKt:yamlKt",
    "net.mamoe.yamlKt:yamlKt-common",
    "net.mamoe.yamlKt:yamlKt-metadata",
    "net.mamoe.yamlKt:yamlKt-jvm",

    "net.mamoe:kotlin-jvm-blocking-bridge",
    "net.mamoe:kotlin-jvm-blocking-bridge-common",
    "net.mamoe:kotlin-jvm-blocking-bridge-metadata",
    "net.mamoe:kotlin-jvm-blocking-bridge-jvm"
).map { it.toLowerCase() }
    .map { MiraiConsoleExtension.ExcludedDependency(it.substringBefore(':'), it.substringAfterLast(':')) }
    .toTypedArray()