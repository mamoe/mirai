/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet

import kotlinx.io.core.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.packet.chat.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.list.ProfileService
import net.mamoe.mirai.internal.network.protocol.packet.login.ConfigPushSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.Heartbeat
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.network.readUShortLVByteArray
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.crypto.adjustToPublicKey
import net.mamoe.mirai.utils.*
import network.protocol.packet.list.StrangerList

internal sealed class PacketFactory<TPacket : Packet?> {
    /**
     * 筛选从服务器接收到的包时的 commandName
     */
    abstract val receivingCommandName: String

    open val canBeCached: Boolean get() = true
}

/**
 * 一种客户端主动发送的数据包的处理工厂.
 * 它必须是由客户端主动发送, 产生一个 sequenceId, 然后服务器以相同的 sequenceId 返回.
 * 必须在 [KnownPacketFactories] 中注册工厂, 否则将不能收到回复.
 * 应由一个 `object` 实现, 且实现 `operator fun invoke` 或按 subCommand 或其意义命名的函数来构造 [OutgoingPacket]
 *
 * @param TPacket 服务器回复包解析结果
 */
internal abstract class OutgoingPacketFactory<TPacket : Packet?>(
    /**
     * 命令名. 如 `wtlogin.login`, `ConfigPushSvc.PushDomain`
     */
    val commandName: String
) : PacketFactory<TPacket>() {
    final override val receivingCommandName: String get() = commandName

    /**
     * **解码**服务器的回复数据包. 返回的包若是 [Event], 则会 broadcast.
     */
    abstract suspend fun ByteReadPacket.decode(bot: QQAndroidBot): TPacket

    /**
     * 可选的处理这个包. 可以在这里面发新的包.
     */
    open suspend fun QQAndroidBot.handle(packet: TPacket) {}
}

/**
 * 处理服务器发来的包的工厂.
 * 这个工厂可以在 [handle] 时回复一个 commandId 为 [responseCommandName] 的包, 也可以不回复.
 * 必须先到 [KnownPacketFactories] 中注册工厂, 否则不能处理.
 */
internal abstract class IncomingPacketFactory<TPacket : Packet?>(
    /**
     * 接收自服务器的包的 commandName
     */
    override val receivingCommandName: String,
    /**
     * 要返回给服务器的包的 commandName
     */
    val responseCommandName: String = ""
) : PacketFactory<TPacket>() {
    /**
     * **解码**服务器的回复数据包. 返回的包若是 [Event], 则会 broadcast.
     */
    abstract suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): TPacket

    /**
     * 处理解码后的包, 返回一个 [OutgoingPacket] 以发送给服务器, 返回 null 则不作处理.
     */
    open suspend fun QQAndroidBot.handle(packet: TPacket, sequenceId: Int): OutgoingPacket? {
        return null
    }
}

@JvmName("decode0")
private suspend inline fun <P : Packet?> OutgoingPacketFactory<P>.decode(bot: QQAndroidBot, packet: ByteReadPacket): P =
    packet.decode(bot)

@JvmName("decode1")
private suspend inline fun <P : Packet?> IncomingPacketFactory<P>.decode(
    bot: QQAndroidBot,
    packet: ByteReadPacket,
    sequenceId: Int
): P =
    packet.decode(bot, sequenceId)

internal val DECRYPTER_16_ZERO = ByteArray(16)

internal typealias PacketConsumer<T> = suspend (packetFactory: PacketFactory<T>, packet: T, commandName: String, ssoSequenceId: Int) -> Unit

/**
 * 数据包相关的调试输出.
 * 它默认是关闭的.
 */
@PublishedApi
internal val PacketLogger: MiraiLoggerWithSwitch by lazy {
    MiraiLogger.create("Packet").withSwitch(false)
}

internal object KnownPacketFactories {
    object OutgoingFactories : List<OutgoingPacketFactory<*>> by mutableListOf(
        WtLogin.Login,
        StatSvc.Register,
        StatSvc.GetOnlineStatus,
        StatSvc.GetDevLoginInfo,
        MessageSvcPbGetMsg,
        MessageSvcPushForceOffline,
        MessageSvcPbSendMsg,
        MessageSvcPbDeleteMsg,
        FriendList.GetFriendGroupList,
        FriendList.DelFriend,
        FriendList.GetTroopListSimplify,
        FriendList.GetTroopMemberList,
        ImgStore.GroupPicUp,
        PttStore.GroupPttUp,
        PttStore.GroupPttDown,
        LongConn.OffPicUp,
        LongConn.OffPicDown,
        TroopManagement.EditSpecialTitle,
        TroopManagement.Mute,
        TroopManagement.GroupOperation,
        //  TroopManagement.GetGroupInfo,
        TroopManagement.EditGroupNametag,
        TroopManagement.Kick,
        NudgePacket,
        Heartbeat.Alive,
        PbMessageSvc.PbMsgWithDraw,
        MultiMsg.ApplyUp,
        NewContact.SystemMsgNewFriend,
        NewContact.SystemMsgNewGroup,
        ProfileService.GroupMngReq,
        StrangerList.GetStrangerList,
        StrangerList.DelStranger
    )

    object IncomingFactories : List<IncomingPacketFactory<*>> by mutableListOf(
        OnlinePushPbPushGroupMsg,
        OnlinePushReqPush,
        OnlinePushPbPushTransMsg,
        MessageSvcPushNotify,
        MessageSvcPushReaded,
        ConfigPushSvc.PushReq,
        StatSvc.ReqMSFOffline,
        StatSvc.SvcReqMSFLoginNotify
    )
    // SvcReqMSFLoginNotify 自己的其他设备上限
    // MessageSvcPushReaded 电脑阅读了别人的消息, 告知手机
    // OnlinePush.PbC2CMsgSync 电脑发消息给别人, 同步给手机

    @Suppress("MemberVisibilityCanBePrivate") // debugging use
    fun findPacketFactory(commandName: String): PacketFactory<*>? {
        return OutgoingFactories.firstOrNull { it.receivingCommandName == commandName }
            ?: IncomingFactories.firstOrNull { it.receivingCommandName == commandName }
    }

    class PacketFactoryIllegalStateException @JvmOverloads constructor(
        val code: Int,
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : RuntimeException()

    // do not inline. Exceptions thrown will not be reported correctly
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Packet?> parseIncomingPacket(
        bot: QQAndroidBot,
        rawInput: ByteReadPacket,
        consumer: PacketConsumer<T>
    ): Unit = with(rawInput) {
        // login
        val flag1 = readInt()

        PacketLogger.verbose { "开始处理一个包" }

        val flag2 = readByte().toInt()
        val flag3 = readByte().toInt()
        check(flag3 == 0) {
            "Illegal flag3. Expected 0, whereas got $flag3. flag1=$flag1, flag2=$flag2. " +
                    "Remaining=${this.readBytes().toUHexString()}"
        }

        readString(readInt() - 4)// uinAccount

        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        ByteArrayPool.useInstance(this.remaining.toInt()) { data ->
            val size = this.readAvailable(data)

            kotlin.runCatching {
                when (flag2) {
                    2 -> TEA.decrypt(data, DECRYPTER_16_ZERO, size)
                    1 -> TEA.decrypt(data, bot.client.wLoginSigInfo.d2Key, size)
                    0 -> data
                    else -> error("")
                }
            }.getOrElse {
                bot.client.tryDecryptOrNull(data, size) { it }
            }?.toReadPacket()?.let { decryptedData ->
                when (flag1) {
                    0x0A -> parseSsoFrame(bot, decryptedData)
                    0x0B -> parseSsoFrame(bot, decryptedData) // 这里可能是 uni?? 但测试时候发现结构跟 sso 一样.
                    else -> error("unknown flag1: ${flag1.toByte().toUHexString()}")
                }
            }?.let {
                it as IncomingPacket<T>

                if (it.packetFactory is IncomingPacketFactory<T> && it.packetFactory.canBeCached && bot.network.pendingEnabled) {
                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    bot.network.pendingIncomingPackets?.add(it.also {
                        it.consumer = consumer
                        it.flag2 = flag2
                        PacketLogger.info { "Cached ${it.commandName} #${it.sequenceId}" }
                    }) ?: handleIncomingPacket(it, bot, flag2, consumer)
                } else {
                    handleIncomingPacket(it, bot, flag2, consumer)
                }
            } ?: kotlin.run {
                PacketLogger.error { "任何key都无法解密: ${data.take(size).toUHexString()}" }
                return
            }
        }
    }

    internal suspend fun <T : Packet?> handleIncomingPacket(
        it: IncomingPacket<T>,
        bot: QQAndroidBot,
        flag2: Int,
        consumer: PacketConsumer<T>
    ) {
        if (it.packetFactory == null) {
            bot.network.logger.debug { "Received unknown commandName: ${it.commandName}" }
            PacketLogger.warning { "找不到 PacketFactory" }
            PacketLogger.verbose {
                "传递给 PacketFactory 的数据 = ${
                    it.data.useBytes { data, length ->
                        data.toUHexString(
                            length = length
                        )
                    }
                }"
            }
            return
        }

        PacketLogger.info { "Handle packet: ${it.commandName}" }
        it.data.withUse {
            when (flag2) {
                0, 1 -> when (it.packetFactory) {
                    is OutgoingPacketFactory<*> -> consumer(
                        it.packetFactory as OutgoingPacketFactory<T>,
                        it.packetFactory.run { decode(bot, it.data) },
                        it.packetFactory.commandName,
                        it.sequenceId
                    )
                    is IncomingPacketFactory<*> -> consumer(
                        it.packetFactory as IncomingPacketFactory<T>,
                        it.packetFactory.run { decode(bot, it.data, it.sequenceId) },
                        it.packetFactory.receivingCommandName,
                        it.sequenceId
                    )
                }

                2 -> it.data.parseOicqResponse(
                    bot,
                    it.packetFactory as OutgoingPacketFactory<T>,
                    it.sequenceId,
                    consumer
                )
                else -> error(
                    "unknown flag2: $flag2. Body to be parsed for inner packet=${it.data.readBytes().toUHexString()}"
                )
            }
        }
    }

    class IncomingPacket<T : Packet?>(
        val packetFactory: PacketFactory<T>?,
        val sequenceId: Int,
        val data: ByteReadPacket,
        val commandName: String
    ) {
        var flag2: Int = -1
        lateinit var consumer: PacketConsumer<T>
    }

    private fun parseSsoFrame(bot: QQAndroidBot, input: ByteReadPacket): IncomingPacket<*> {
        val commandName: String
        val ssoSequenceId: Int
        val dataCompressed: Int
        input.readPacketExact(input.readInt() - 4).withUse {
            ssoSequenceId = readInt()
            PacketLogger.verbose { "sequenceId = $ssoSequenceId" }

            val returnCode = readInt()
            check(returnCode == 0) {
                if (returnCode <= -10000) {
                    // https://github.com/mamoe/mirai/issues/470
                    throw PacketFactoryIllegalStateException(returnCode, "returnCode = $returnCode")
                } else "returnCode = $returnCode"
            }

            if (PacketLogger.isEnabled) {
                val extraData = readBytes(readInt() - 4)
                PacketLogger.verbose { "(sso/inner)extraData = ${extraData.toUHexString()}" }
            } else {
                discardExact(readInt() - 4)
            }

            commandName = readString(readInt() - 4)
            bot.client.outgoingPacketSessionId = readBytes(readInt() - 4)

            dataCompressed = readInt()
        }

        val packet = when (dataCompressed) {
            0 -> {
                val size = input.readInt().toLong() and 0xffffffff
                if (size == input.remaining || size == input.remaining + 4) {
                    input
                } else {
                    buildPacket {
                        writeInt(size.toInt())
                        writePacket(input)
                    }
                }
            }
            1 -> {
                input.discardExact(4)
                input.useBytes { data, length ->
                    data.unzip(0, length).let {
                        val size = it.toInt()
                        if (size == it.size || size == it.size + 4) {
                            it.toReadPacket(offset = 4)
                        } else {
                            it.toReadPacket()
                        }
                    }
                }
            }
            8 -> input
            else -> error("unknown dataCompressed flag: $dataCompressed")
        }

        // body
        val packetFactory = findPacketFactory(commandName)


        return IncomingPacket(packetFactory, ssoSequenceId, packet, commandName)
    }

    private suspend fun <T : Packet?> ByteReadPacket.parseOicqResponse(
        bot: QQAndroidBot,
        packetFactory: OutgoingPacketFactory<T>,
        ssoSequenceId: Int,
        consumer: PacketConsumer<T>
    ) {
        @Suppress("DuplicatedCode")
        check(readByte().toInt() == 2)
        this.discardExact(2)
        this.discardExact(2)
        this.readUShort()
        this.readShort()
        this.readUInt().toLong()
        val encryptionMethod = this.readUShort().toInt()

        this.discardExact(1)
        val packet = when (encryptionMethod) {
            4 -> {
                var data = TEA.decrypt(this, bot.client.ecdh.keyPair.initialShareKey, (this.remaining - 1).toInt())

                val peerShareKey =
                    bot.client.ecdh.calculateShareKeyByPeerPublicKey(readUShortLVByteArray().adjustToPublicKey())
                data = TEA.decrypt(data, peerShareKey)

                packetFactory.decode(bot, data)
            }
            0 -> {
                val data = if (bot.client.loginState == 0) {
                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    ByteArrayPool.useInstance(this.remaining.toInt()) { byteArrayBuffer ->
                        val size = (this.remaining - 1).toInt()
                        this.readFully(byteArrayBuffer, 0, size)

                        runCatching {
                            TEA.decrypt(byteArrayBuffer, bot.client.ecdh.keyPair.initialShareKey, size)
                        }.getOrElse {
                            TEA.decrypt(byteArrayBuffer, bot.client.randomKey, size)
                        }.toReadPacket()
                    }
                } else {
                    TEA.decrypt(this, bot.client.randomKey, 0, (this.remaining - 1).toInt())
                }

                packetFactory.decode(bot, data)

            }
            else -> error("Illegal encryption method. expected 0 or 4, got $encryptionMethod")
        }

        consumer(packetFactory, packet, packetFactory.commandName, ssoSequenceId)
    }

}