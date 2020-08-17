/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

object Versions {
    object Mirai {
        const val version = "1.2.0"
    }

    object Kotlin {
        const val compiler = "1.4.0-rc"
        const val stdlib = "1.4.0-rc"
        const val coroutines = "1.3.8-1.4.0-rc"
        const val atomicFU = "0.14.3-1.4.0-rc"
        const val serialization = "1.0-M1-1.4.0-rc"
        const val ktor = "1.3.2-1.4.0-rc"
        const val binaryValidator = "0.2.3"

        const val io = "0.1.16"
        const val coroutinesIo = "0.1.16"
        const val dokka = "0.10.1"
    }

    const val jcekt = "2.0.0-1.4.0-rc-4"

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
fun ktor(id: String, version: String = Versions.Kotlin.ktor) = "io.ktor:ktor-$id:$version"
