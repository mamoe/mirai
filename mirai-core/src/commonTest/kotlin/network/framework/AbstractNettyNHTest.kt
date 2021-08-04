/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.MockConfiguration
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.SharedRandomProvider
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.handler.*
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler
import net.mamoe.mirai.internal.network.recording.PacketRecordBundle
import net.mamoe.mirai.internal.network.recording.ReplayingPacketHandler
import net.mamoe.mirai.internal.network.recording.loadFrom
import net.mamoe.mirai.utils.ExceptionCollector
import net.mamoe.mirai.utils.warning
import java.net.SocketAddress
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/**
 * You may need to override [createConnection]
 */
internal abstract class TestNettyNH(
    override val bot: QQAndroidBot,
    context: NetworkHandlerContext,
    address: SocketAddress,
) : NettyNetworkHandler(context, address), ITestNetworkHandler {

    abstract override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel

    override fun setStateClosed(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateClosed(exception) }
    }

    override fun setStateConnecting(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateConnecting(ExceptionCollector(exception)) }
    }

    override fun setStateOK(channel: Channel, exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        exception?.printStackTrace()
        return setState { StateOK(channel, CompletableDeferred(Unit)) }
    }

    override fun setStateLoading(channel: Channel): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateLoading(channel) }
    }

}

/**
 * Without selector. When network is closed, it will not reconnect, so that you can check for its states.
 *
 * @see AbstractNettyNHTestWithSelector
 */
internal abstract class AbstractNettyNHTest : AbstractRealNetworkHandlerTest<TestNettyNH>() {

    init {
        overrideComponents[BotOfflineEventMonitor] = object : BotOfflineEventMonitor {
            override fun attachJob(bot: AbstractBot, scope: CoroutineScope) {
            }
        }
    }

    var channel: EmbeddedChannel = EmbeddedChannel()

    override val network: TestNettyNH get() = bot.network as TestNettyNH

    override val factory: NetworkHandlerFactory<TestNettyNH> =
        NetworkHandlerFactory<TestNettyNH> { context, address ->
            object : TestNettyNH(bot, context, address) {
                override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel =
                    channel.apply {
                        pipeline().removeAll { true }
                        setupChannelPipeline(pipeline(), decodePipeline)
                    }
            }
        }

    /**
     * Can be called only once.
     */
    fun useRecording(url: URL) {
        overrideComponents.keys.forEach { if (it != ServerList) overrideComponents.remove(it) }

        val handler = ReplayingPacketHandler(PacketRecordBundle.loadFrom(url.readBytes()))

        networkLogger.warning {
            handler.bundle.run {
                """
                Loaded recording ${url.file}:
                Version: $version
                Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(time))}
                Count: ${records.size}
                Seed: $seed
                Note: $note
            """.trimIndent()
            }
        }
        bot = object : QQAndroidBot(TODO(), MockConfiguration.copy()) {
            override fun createBotLevelComponents(): ConcurrentComponentStorage =
                super.createBotLevelComponents().apply {
                    this[PacketHandler] += handler
                    this[RandomProvider] = SharedRandomProvider(Random(handler.bundle.seed))
                }

            override fun createNetworkHandler(): NetworkHandler =
                factory.create(
                    NetworkHandlerContextImpl(bot, networkLogger, createNetworkLevelComponents()),
                    createAddress()
                )
        }
        channel = handler.channel
    }

    /**
     * Can be called only once.
     */
    fun useRecording(name: String) {
        return useRecording(
            this::class.java.classLoader.getResource(name)
                ?: this::class.java.classLoader.getResource("recording/data/$name")
                ?: error("Could not find recording '$name'")
        )
    }
}
