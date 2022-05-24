/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import io.ktor.utils.io.core.*
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.ReferenceCountUtil
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.error

internal class NettyNHTestChannel(
    var fakeServer: (NettyNHTestChannel.(msg: Any?) -> Unit)?,
) : EmbeddedChannel() {
    constructor() : this(null)

    @OptIn(InternalSerializationApi::class)
    fun listen(listener: (OutgoingPacket) -> Any?) {
        fakeServer = { packet ->
            if (packet is OutgoingPacket) {
                val rsp0 = when (val rsp = listener(packet)) {
                    null -> null
                    is Unit -> null
                    is ByteArray -> {
                        RawIncomingPacket(
                            commandName = packet.commandName,
                            sequenceId = packet.sequenceId,
                            body = rsp
                        )
                    }
                    is RawIncomingPacket -> rsp
                    is ProtoBuf -> {
                        RawIncomingPacket(
                            commandName = packet.commandName,
                            sequenceId = packet.sequenceId,
                            body = buildPacket {
                                writeProtoBuf(
                                    rsp::class.serializer().cast<KSerializer<ProtoBuf>>(),
                                    rsp
                                )
                            }.readBytes()
                        )
                    }
                    else -> {
                        logger.error { "Failed to respond $rsp" }
                        null
                    }
                }
                if (rsp0 != null) {
                    pipeline().fireChannelRead(rsp0)
                }
            }
            ReferenceCountUtil.release(packet)
        }
    }

    public /*internal*/ override fun doRegister() {
        super.doRegister() // Set channel state to ACTIVE
        // Drop old handlers
        pipeline().let { p ->
            while (p.first() != null) {
                p.removeFirst()
            }
        }
    }

    override fun handleInboundMessage(msg: Any?) {
        ReferenceCountUtil.release(msg) // Not handled, Drop
    }

    override fun handleOutboundMessage(msg: Any?) {
        fakeServer?.invoke(this, msg) ?: ReferenceCountUtil.release(msg)
    }

    companion object {
        private val logger by lazy {
            MiraiLogger.Factory.create(NettyNHTestChannel::class)
        }
    }
}