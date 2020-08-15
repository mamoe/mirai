/*
 * Copyright 2020 Mamoe Technologies and contributors.
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */


plugins {
    java
}


dependencies {

    implementation(project(":mirai-core"))

    implementation(project(":mirai-serialization"))

    testImplementation(group = "junit", name = "junit", version = "4.12")

    implementation(kotlin("stdlib", null))
    implementation(kotlin("serialization", null))
    implementation(kotlin("reflect", null))


    implementation(kotlinx("serialization-runtime-common", Versions.Kotlin.serialization))
    implementation(kotlinx("serialization-protobuf-common", Versions.Kotlin.serialization))
    implementation(kotlinx("io", Versions.Kotlin.io))
    implementation(kotlinx("coroutines-io", Versions.Kotlin.coroutinesIo))
    implementation(kotlinx("coroutines-core-common", Versions.Kotlin.coroutines))

    implementation("org.jetbrains.kotlinx:atomicfu-common:${Versions.Kotlin.atomicFU}")

    implementation(ktor("client-cio", Versions.Kotlin.ktor))
    implementation(ktor("client-core", Versions.Kotlin.ktor))
    implementation(ktor("network", Versions.Kotlin.ktor))

}
