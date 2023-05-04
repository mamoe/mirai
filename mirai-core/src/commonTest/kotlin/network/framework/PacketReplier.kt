/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import kotlin.jvm.JvmName

/**
 * 应答器, 模拟服务器返回.
 */
internal fun interface PacketReplier {
    fun PacketReplierContext.onSend(packet: OutgoingPacket)
}

internal inline fun buildPacketReplier(crossinline builderAction: PacketReplierDslBuilder.() -> Unit): PacketReplier {
    return PacketReplierDslBuilder().apply { builderAction() }.build()
}

internal interface PacketReplierContext {
    val context: PacketReplierContext get() = this
    val coroutineScope: CoroutineScope

    fun reply(incoming: IncomingPacket)
    fun reply(incoming: Packet)
    fun reply(incoming: Throwable)
    fun ignore() {}
}


internal sealed class PacketReplierDecision {
    data class Reply(val action: PacketReplierContext.(outgoingPacket: OutgoingPacket) -> Unit) :
        PacketReplierDecision()

    data object Ignore : PacketReplierDecision()
}

internal class PacketReplierDslBuilder {
    val decisions: MutableList<PacketReplierDecision> = mutableListOf()

    class On<T : Packet?>(
        val fromFactories: List<OutgoingPacketFactory<T>>,
    )

    /**
     * Expects the next packet to be exactly
     */
    fun <T : Packet?> expect(
        vararg from: OutgoingPacketFactory<T>,
    ): On<T> = On(from.toList())

    fun <T : Packet?> expect(
        vararg from: OutgoingPacketFactory<T>,
        action: PacketReplierContext.(outgoingPacket: OutgoingPacket) -> Unit
    ): Unit = On(from.toList()).invoke(action)

    operator fun <T : Packet?> On<T>.invoke(
        action: PacketReplierContext.(outgoingPacket: OutgoingPacket) -> Unit
    ) {
        decisions.add(PacketReplierDecision.Reply { outgoing ->
            fromFactories
                .find { it.commandName == outgoing.commandName }
                ?.let {
                    return@Reply action(this, outgoing)
                }
                ?: run {
                    val factories = fromFactories.joinToString(prefix = "[", postfix = "]") { it.commandName }
                    throw AssertionError(
                        "Expected client to send a packet from factories $factories, but client sent ${outgoing.commandName}"
                    )
                }
        })
    }

    @JvmName("replyPacket")
    @OverloadResolutionByLambdaReturnType
    inline infix fun <T : Packet?> On<T>.reply(crossinline lazyIncoming: () -> Packet) {
        invoke { context.reply(lazyIncoming()) }
    }

    @JvmName("replyIncomingPacket")
    @OverloadResolutionByLambdaReturnType
    inline infix fun <T : Packet?> On<T>.reply(crossinline lazyIncoming: () -> IncomingPacket) {
        invoke { context.reply(lazyIncoming()) }
    }

    @JvmName("replyThrowable")
    @OverloadResolutionByLambdaReturnType
    inline infix fun <T : Packet?> On<T>.reply(crossinline lazyIncoming: () -> Throwable) {
        invoke { context.reply(lazyIncoming()) }
    }

    inline infix fun <T : Packet?> On<T>.ignore(crossinline lazy: () -> Unit) {
        invoke {
            lazy()
            context.ignore()
        }
    }


    /**
     * Ignore the next packet.
     */
    fun ignore() {
        decisions.add(PacketReplierDecision.Ignore)
    }

    internal fun build(): PacketReplier {
        return PacketReplier { outgoing ->
            val context = this
            coroutineScope.launch {
                when (val decision =
                    decisions.removeFirstOrNull()
                        ?: throw AssertionError("Client sent a packet ${outgoing.commandName} while not expected")
                ) {
                    is PacketReplierDecision.Ignore -> return@launch
                    is PacketReplierDecision.Reply -> {
                        decision.action.invoke(context, outgoing)
                    }
                }
            }
        }
    }
}