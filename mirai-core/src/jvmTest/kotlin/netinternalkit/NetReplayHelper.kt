/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("NetReplayHelper")
@file:Suppress("TestFunctionName")

package net.mamoe.mirai.internal.netinternalkit

import io.netty.channel.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.components.PacketLoggingStrategyImpl
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.components.ServerList
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContextImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.SocketAddress
import net.mamoe.mirai.internal.network.handler.selector.KeepAliveNetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.*
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.invoke.MethodHandles
import javax.swing.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


internal object NetReplayHelperSettings {
    var commands_hide_hideAll: Collection<String> by lateinitMutableProperty {
        listOf(
            "Heartbeat.Alive",
            "wtlogin.exchange_emp",
            "StatSvc.register",
            "StatSvc.GetDevLoginInfo",
            "MessageSvc.PbGetMsg",
            "friendlist.getFriendGroupList",
            "friendlist.GetTroopListReqV2",
            "friendlist.GetTroopMemberListReq",
        )
    }

    var commands_hide_hideInConsole: Collection<String> by lateinitMutableProperty {
        listOf(
            "ConfigPushSvc.PushReq",
            *PacketLoggingStrategyImpl.getDefaultBlacklist().toTypedArray(),
        )
    }

    var logger_console: MiraiLogger by lateinitMutableProperty {
        MiraiLogger.Factory.create(NetReplayHelperClass())
    }

    var logger_file: MiraiLogger = SilentLogger.withSwitch(false)

    @JvmField
    val NetReplyHelper: Class<*> = NetReplayHelperClass()
}

private fun NetReplayHelperClass(): Class<*> {
    return MethodHandles.lookup().lookupClass()
}


private fun attachNetReplayHelper(channel: Channel) {
    channel.pipeline() // TODO: 2022/6/2 will not work since "raw-packet-collector" has been removed
        .addBefore("raw-packet-collector", "raw-packet-dumper", newRawPacketDumper())

    attachNetReplayWView(channel)
}

private fun newRawPacketDumper(): ChannelHandler = object : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is RawIncomingPacket) {
            if (msg.commandName in NetReplayHelperSettings.commands_hide_hideAll) {
                NetReplayHelperSettings.logger_console.debug {
                    "sid=${msg.sequenceId}, cmd=${msg.commandName}, body=<DROPPED>"
                }
                NetReplayHelperSettings.logger_file.debug {
                    "sid=${msg.sequenceId}, cmd=${msg.commandName}, body=<DROPPED>"
                }
                super.channelRead(ctx, msg)
                return
            }
            if (msg.commandName in NetReplayHelperSettings.commands_hide_hideInConsole) {
                NetReplayHelperSettings.logger_console.debug {
                    "sid=${msg.sequenceId}, cmd=${msg.commandName}, body=<DROPPED>"
                }
            } else {
                NetReplayHelperSettings.logger_console.debug {
                    "sid=${msg.sequenceId}, cmd=${msg.commandName}, body=${msg.body.toUHexString()}"
                }
            }
            NetReplayHelperSettings.logger_file.debug {
                "sid=${msg.sequenceId}, cmd=${msg.commandName}, body=${msg.body.toUHexString()}"
            }
        }
        super.channelRead(ctx, msg)
    }
}

private fun attachNetReplayWView(channel: Channel) {
    val frame = JFrame("Net Replay Helper")
    val panel = JPanel()
    val layout = GroupLayout(panel)
    panel.layout = layout
    frame.add(panel)

    val cmd = JTextField()
    val sid = JTextField()
    val bdy = JTextField()
    val log = JTextField()

    val cmdLabel = JLabel("cmd").also { it.labelFor = cmd }
    val sidLabel = JLabel("seq").also { it.labelFor = sid }
    val bdyLabel = JLabel("body").also { it.labelFor = bdy }
    val logLabel = JLabel("log").also { it.labelFor = log }

    val fireCustom = JButton("Fire Cus")
    val fireLog = JButton("Fire Log")

    // region
    layout.setHorizontalGroup(
        layout.createParallelGroup().addGroup(
            layout.createSequentialGroup().addGroup(
                layout.createParallelGroup()
                    .addComponent(cmdLabel)
                    .addComponent(sidLabel)
                    .addComponent(bdyLabel)
                    .addComponent(logLabel)
            ).addGroup(
                layout.createParallelGroup()
                    .addComponent(cmd)
                    .addComponent(sid)
                    .addComponent(bdy)
                    .addComponent(log)
            )
        ).addGroup(
            layout.createSequentialGroup()
                .addComponent(fireCustom)
                .addComponent(fireLog)
        )
    )
    layout.setVerticalGroup(
        layout.createSequentialGroup()
            .addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdLabel)
                    .addComponent(cmd)
            )
            .addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sidLabel)
                    .addComponent(sid)
            )
            .addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bdyLabel)
                    .addComponent(bdy)
            )
            .addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(logLabel)
                    .addComponent(log)
            )
            .addGroup(
                layout.createParallelGroup()
                    .addComponent(fireCustom)
                    .addComponent(fireLog)
            )
    )
    // endregion

    fun Component.onClick(handle: () -> Unit) {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                runCatching(handle).onFailure { err ->
                    NetReplayHelperSettings.logger_console.error(err)
                    NetReplayHelperSettings.logger_file.error(err)
                }
            }
        })
    }

    fireCustom.onClick {
        val rp = RawIncomingPacket(
            commandName = cmd.text.trim(),
            sequenceId = sid.text.toInt(),
            body = bdy.text.hexToBytes(),
        )
        channel.pipeline().fireChannelRead(rp)
    }

    @Suppress("LocalVariableName")
    fireLog.onClick {
        var line = log.text.substringAfter("sid=")
        // 2021-11-07 11:49:38 D/NetReplayHelper: sid=123, cmd=HelloWorld!, body=00
        val sid_ = line.substringBefore(",").toInt()
        line = line.substringAfter("cmd=")
        val cmd_ = line.substringBeforeLast("body=").trim().removeSuffix(",").trim()
        val bdy_ = line.substringAfterLast("body=").trim().hexToBytes()

        val rp = RawIncomingPacket(
            commandName = cmd_,
            sequenceId = sid_,
            body = bdy_,
        )
        channel.pipeline().fireChannelRead(rp)
    }

    frame.pack()
    frame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
    frame.setLocationRelativeTo(null)
    frame.isVisible = true

    channel.closeFuture().addListener {
        SwingUtilities.invokeLater {
            frame.dispose()
        }
    }

}

private object NRHNettyNetworkHandlerFactory : NetworkHandlerFactory<NettyNetworkHandler> {
    override fun create(context: NetworkHandlerContext, address: SocketAddress): NettyNetworkHandler {
        return object : NettyNetworkHandler(context, address) {
            override fun setupChannelPipeline(pipeline: ChannelPipeline, decodePipeline: PacketDecodePipeline) {
                super.setupChannelPipeline(pipeline, decodePipeline)
                attachNetReplayHelper(pipeline.channel())
            }
        }
    }
}

// Call before bot.login()
fun Bot.attachNetReplayHelper() {
    asQQAndroidBot()
    val networkLogger = this::class.declaredMembers.first { it.name == "networkLogger" }.let { property ->
        property as KProperty<*>
        property.isAccessible = true
        property.getter.call(this@attachNetReplayHelper)
    } as MiraiLogger


    val snh = network.cast<SelectorNetworkHandler<*>>()
    val field = snh::selector.javaField!!
    field.isAccessible = true
    field.set(
        snh,
        KeepAliveNetworkHandlerSelector(
            maxAttempts = configuration.reconnectionRetryTimes.coerceIn(1, Int.MAX_VALUE),
            logger = networkLogger.subLogger("Selector")
        ) {
            val context = NetworkHandlerContextImpl(
                bot,
                networkLogger,
                createNetworkLevelComponents(),
            )
            NRHNettyNetworkHandlerFactory.create(
                context,
                context[ServerList].pollAny().toSocketAddress(),
            )
        },
    )

}

fun main() {
    val bot = BotFactory.newBot(0, "")
    bot.attachNetReplayHelper() //
    // TODO: 2022/6/2 will not work since "raw-packet-collector" has been removed, see net.mamoe.mirai.internal.netinternalkit.NetReplayHelper.attachNetReplayHelper(io.netty.channel.Channel)
}
