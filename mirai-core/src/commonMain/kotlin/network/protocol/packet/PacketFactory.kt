/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.PacketCodec
import net.mamoe.mirai.internal.network.protocol.packet.chat.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.list.ProfileService
import net.mamoe.mirai.internal.network.protocol.packet.list.StrangerList
import net.mamoe.mirai.internal.network.protocol.packet.login.ConfigPushSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.Heartbeat
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.network.protocol.packet.summarycard.SummaryCard
import net.mamoe.mirai.utils.MiraiLoggerWithSwitch

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
internal suspend inline fun <P : Packet?> OutgoingPacketFactory<P>.decode(
    bot: QQAndroidBot,
    packet: ByteReadPacket
): P =
    packet.decode(bot)

@JvmName("decode1")
internal suspend inline fun <P : Packet?> IncomingPacketFactory<P>.decode(
    bot: QQAndroidBot,
    packet: ByteReadPacket,
    sequenceId: Int
): P = packet.decode(bot, sequenceId)

/**
 * 数据包相关的调试输出.
 * 它默认是关闭的.
 */
@Deprecated(
    "Kept for binary compatibility.",
    ReplaceWith("PacketCodec.PacketLogger", "net.mamoe.mirai.internal.network.components.PacketCodec"),
    level = DeprecationLevel.ERROR,
)
@PublishedApi
internal val PacketLogger: MiraiLoggerWithSwitch
    get() = PacketCodec.PacketLogger

/**
 * Registered factories.
 */
internal object KnownPacketFactories {
    object OutgoingFactories : List<OutgoingPacketFactory<*>> by mutableListOf(
        WtLogin.Login,
        WtLogin.ExchangeEmp,
        StatSvc.Register,
        StatSvc.GetOnlineStatus,
        StatSvc.SimpleGet,
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
        TroopManagement.GetTroopConfig,
        TroopManagement.ModifyAdmin,
        //  TroopManagement.GetGroupInfo,
        TroopManagement.EditGroupNametag,
        TroopManagement.Kick,
        TroopEssenceMsgManager.SetEssence,
        NudgePacket,
        Heartbeat.Alive,
        PbMessageSvc.PbMsgWithDraw,
        MultiMsg.ApplyUp,
        MultiMsg.ApplyDown,
        NewContact.SystemMsgNewFriend,
        NewContact.SystemMsgNewGroup,
        ProfileService.GroupMngReq,
        StrangerList.GetStrangerList,
        StrangerList.DelStranger,
        SummaryCard.ReqSummaryCard,
        MusicSharePacket,
        *FileManagement.factories
    )

    object IncomingFactories : List<IncomingPacketFactory<*>> by mutableListOf(
        OnlinePushPbPushGroupMsg,
        OnlinePushReqPush,
        OnlinePushPbPushTransMsg,
        OnlinePushSidExpired,
        MessageSvcPushNotify,
        MessageSvcPushReaded,
        MessageSvcRequestPushStatus,
        ConfigPushSvc.PushReq,
        PbC2CMsgSync,
        StatSvc.ReqMSFOffline,
        StatSvc.SvcReqMSFLoginNotify
    )
    // SvcReqMSFLoginNotify 自己的其他设备上限
    // MessageSvcPushReaded 电脑阅读了别人的消息, 告知手机
    // OnlinePush.PbC2CMsgSync 电脑发消息给别人, 同步给手机

    fun findPacketFactory(commandName: String): PacketFactory<*>? {
        return OutgoingFactories.firstOrNull { it.receivingCommandName == commandName }
            ?: IncomingFactories.firstOrNull { it.receivingCommandName == commandName }
    }
}
