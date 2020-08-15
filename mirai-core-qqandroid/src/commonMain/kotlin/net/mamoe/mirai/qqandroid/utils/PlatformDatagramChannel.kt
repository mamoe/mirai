/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable

/**
 * 多平台适配的 DatagramChannel.
 */
internal expect class PlatformDatagramChannel(serverHost: String, serverPort: Short) : Closeable {
    /**
     * @throws SendPacketInternalException
     */
    suspend inline fun send(packet: ByteReadPacket): Boolean

    /**
     * @throws ReadPacketInternalException
     */
    suspend inline fun read(): ByteReadPacket

    val isOpen: Boolean
}

/**
 * 在 [PlatformDatagramChannel.send] 或 [PlatformDatagramChannel.read] 时出现的错误.
 */
internal class SendPacketInternalException(cause: Throwable?) : Exception(cause)

/**
 * 在 [PlatformDatagramChannel.send] 或 [PlatformDatagramChannel.read] 时出现的错误.
 */
internal class ReadPacketInternalException(cause: Throwable?) : Exception(cause)