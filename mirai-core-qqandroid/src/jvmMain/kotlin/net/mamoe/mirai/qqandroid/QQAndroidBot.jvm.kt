/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.consumeEachBufferRange
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.io.*
import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.qqandroid.utils.ByteArrayPool
import net.mamoe.mirai.qqandroid.utils.toReadPacket
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.ContextImpl
import java.nio.ByteBuffer


@Suppress("FunctionName")
internal fun QQAndroidBot(account: BotAccount, configuration: BotConfiguration): QQAndroidBot =
    QQAndroidBot(ContextImpl(), account, configuration)