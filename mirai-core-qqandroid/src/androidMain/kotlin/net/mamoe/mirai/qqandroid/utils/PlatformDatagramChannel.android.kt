/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.nio.readPacketAtMost
import kotlinx.io.nio.writePacket
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

/**
 * 多平台适配的 DatagramChannel.
 */
internal actual class PlatformDatagramChannel actual constructor(
    serverHost: String,
    serverPort: Short
) : Closeable {
    @PublishedApi
    internal val channel: DatagramChannel =
        DatagramChannel.open().connect(InetSocketAddress(serverHost, serverPort.toInt()))
    actual val isOpen: Boolean get() = channel.isOpen
    override fun close() = channel.close()

    actual suspend inline fun send(packet: ByteReadPacket): Boolean = withContext(Dispatchers.IO) {
        try {
            (channel as WritableByteChannel).writePacket(packet)
        } catch (e: Throwable) {
            throw SendPacketInternalException(e)
        }
    }

    actual suspend inline fun read(): ByteReadPacket = withContext(Dispatchers.IO) {
        try {
            (channel as ReadableByteChannel).readPacketAtMost(Long.MAX_VALUE)
        } catch (e: Throwable) {
            throw ReadPacketInternalException(e)
        }
    }
}