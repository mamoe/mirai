/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal

import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.client.plugins.*

internal actual fun createDefaultHttpClient(): HttpClient {
    return HttpClient(Curl) {
        install(HttpTimeout) {
            this.requestTimeoutMillis = 30_0000
            this.connectTimeoutMillis = 30_0000
            this.socketTimeoutMillis = 30_0000
        }
    }
}
