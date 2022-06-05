/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.ConcurrentLinkedQueue
import net.mamoe.mirai.utils.TestOnly

/**
 * States are manually set.
 */
internal open class TestNetworkHandler(
    override val bot: QQAndroidBot,
    context: NetworkHandlerContext,
) : NetworkHandlerSupport(context), ITestNetworkHandler<TestNetworkHandler.Connection> {
    class Connection

    @Suppress("EXPOSED_SUPER_CLASS")
    internal abstract inner class TestState(
        correspondingState: NetworkHandler.State
    ) : BaseStateImpl(correspondingState) {
        val resumeDeferred = CompletableDeferred<Unit>()
        val resumeCount = atomic(0)
        val onResume get() = resumeDeferred.onJoin
        private val mutex = Mutex()

        override suspend fun resumeConnection0(): Unit = mutex.withLock {
            resumeCount.incrementAndGet()
            resumeDeferred.complete(Unit)
            when (this.correspondingState) {
                INITIALIZED -> {
                    setState(CONNECTING)
                }
                else -> {
                }
            }
        }

        override fun toString(): String {
            return "TestState($correspondingState)"
        }
    }

    internal inner class TestStateInitial : TestState(INITIALIZED)
    internal inner class TestStateConnecting : TestState(CONNECTING)
    internal inner class TestStateLoading : TestState(LOADING)
    internal inner class TestStateOK : TestState(OK)
    internal inner class TestStateClosed : TestState(CLOSED)

    @OptIn(TestOnly::class)
    fun setState(correspondingState: NetworkHandler.State): TestState? {
        // `null` means ignore checks. All test states have same type TestState.
        val state: TestState = when (correspondingState) {
            INITIALIZED -> TestStateInitial()
            CONNECTING -> TestStateConnecting()
            LOADING -> TestStateLoading()
            OK -> TestStateOK()
            CLOSED -> TestStateClosed()
        }
        return setStateImpl(TestState::class) { state }
    }

    private val initialState = TestStateInitial()
    override fun initialState(): BaseStateImpl = initialState

    val sendPacket get() = ConcurrentLinkedQueue<OutgoingPacket>()

    override suspend fun sendPacketImpl(packet: OutgoingPacket) {
        logger.info("sendPacketImpl: ${packet.displayName}")
        sendPacket.add(packet)
    }


    override fun setStateClosed(exception: Throwable?): TestState? {
        return setState(CLOSED)
    }

    override fun setStateConnecting(exception: Throwable?): TestState? {
        return setState(CONNECTING)
    }

    override fun setStateOK(conn: Connection, exception: Throwable?): TestState? {
        exception?.printStackTrace()
        return setState(OK)
    }

    override fun setStateLoading(conn: Connection): TestState? {
        return setState(LOADING)
    }
}