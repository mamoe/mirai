/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.handler.impl.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.net.protocol.SsoProcessor
import net.mamoe.mirai.internal.network.net.protocol.SsoProcessorContextImpl
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.MiraiLogger
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger


internal class TestNetworkHandlerContext(
    override val bot: QQAndroidBot = MockBot(),
    override val logger: MiraiLogger = MiraiLogger.create("Test"),
    override val ssoProcessor: SsoProcessor = SsoProcessor(SsoProcessorContextImpl(bot)),
) : NetworkHandlerContext

internal open class TestNetworkHandler(
    context: NetworkHandlerContext,
) : NetworkHandlerSupport(context) {
    @Suppress("EXPOSED_SUPER_CLASS")
    internal open inner class TestState(
        correspondingState: NetworkHandler.State
    ) : BaseStateImpl(correspondingState) {
        val resumeDeferred = CompletableDeferred<Unit>()
        val resumeCount = AtomicInteger(0)
        val onResume get() = resumeDeferred.onJoin

        override suspend fun resumeConnection() {
            resumeCount.incrementAndGet()
            resumeDeferred.complete(Unit)
        }
    }

    fun setState(correspondingState: NetworkHandler.State) {
        setState { TestState(correspondingState) }
    }

    private val initialState = TestState(NetworkHandler.State.INITIALIZED)
    override fun initialState(): BaseStateImpl = initialState

    val sendPacket get() = ConcurrentLinkedQueue<OutgoingPacket>()

    override suspend fun sendPacketImpl(packet: OutgoingPacket) {
        sendPacket.add(packet)
    }
}
