/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

object Versions {
    object Mirai {
        const val version = "0.37.5"
    }

    object Kotlin {
        const val stdlib = "1.3.71"
        const val coroutines = "1.3.5" // isn't used
        const val atomicFU = "0.14.2"
        const val serialization = "0.20.0"
        const val ktor = "1.3.2"

        const val io = "0.1.16"
        const val coroutinesIo = "0.1.16"
        const val dokka = "0.10.1"
    }

    object Android {
        const val androidGradlePlugin = "3.5.3"
    }

    object Publishing {
        const val bintray = "1.8.5"
    }

}

@Suppress("unused")
fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

@Suppress("unused")
fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"
