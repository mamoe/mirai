/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import io.ktor.client.*
import net.mamoe.mirai.internal.createDefaultHttpClient
import net.mamoe.mirai.internal.network.component.ComponentKey

internal interface HttpClientProvider {
    fun getHttpClient(): HttpClient

    companion object : ComponentKey<HttpClientProvider>
}

internal class HttpClientProviderImpl : HttpClientProvider {
    private val instance by lazy { createDefaultHttpClient() }
    override fun getHttpClient(): HttpClient = instance
}