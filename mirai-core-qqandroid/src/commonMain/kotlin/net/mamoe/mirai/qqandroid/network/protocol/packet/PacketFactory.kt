package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.OnlinePush
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.ConfigPushSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.cryptor.adjustToPublicKey
import net.mamoe.mirai.utils.cryptor.decryptBy
import net.mamoe.mirai.utils.io.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmName


internal sealed class PacketFactory<TPacket : Packet> {
    /**
     * 筛选从服务器接收到的包时的 commandName
     */
    abstract val receivingCommandName: String
}

/**
 * 一种客户端主动发送的数据包的处理工厂.
 * 它必须是由客户端主动发送, 产生一个 sequenceId, 然后服务器以相同的 sequenceId 返回.
 * 必须在 [KnownPacketFactories] 中注册工厂, 否则将不能收到回复.
 * 应由一个 `object` 实现, 且实现 `operator fun invoke` 或按 subCommand 或其意义命名的函数来构造 [OutgoingPacket]
 *
 * @param TPacket 服务器回复包解析结果
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
internal abstract class OutgoingPacketFactory<TPacket : Packet>(
    /**
     * 命令名. 如 `wtlogin.login`, `ConfigPushSvc.PushDomain`
     */
    val commandName: String
) : PacketFactory<TPacket>() {
    final override val receivingCommandName: String get() = commandName

    /**
     * **解码**服务器的回复数据包. 返回的包若是 [Subscribable], 则会 broadcast.
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
internal abstract class IncomingPacketFactory<TPacket : Packet>(
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
     * **解码**服务器的回复数据包. 返回的包若是 [Subscribable], 则会 broadcast.
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
private suspend inline fun <P : Packet> OutgoingPacketFactory<P>.decode(bot: QQAndroidBot, packet: ByteReadPacket): P = packet.decode(bot)

@JvmName("decode1")
private suspend inline fun <P : Packet> IncomingPacketFactory<P>.decode(bot: QQAndroidBot, packet: ByteReadPacket, sequenceId: Int): P =
    packet.decode(bot, sequenceId)

internal val DECRYPTER_16_ZERO = ByteArray(16)

internal typealias PacketConsumer<T> = suspend (packetFactory: PacketFactory<T>, packet: T, commandName: String, ssoSequenceId: Int) -> Unit

/**
 * 数据包相关的调试输出.
 * 它默认是关闭的.
 */
@PublishedApi
internal val PacketLogger: MiraiLoggerWithSwitch = DefaultLogger("Packet").withSwitch(false)

/**
 * 已知的数据包工厂列表.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
internal object KnownPacketFactories {
    object OutgoingFactories : List<OutgoingPacketFactory<*>> by mutableListOf(
        LoginPacket,
        StatSvc.Register,
        StatSvc.GetOnlineStatus,
        MessageSvc.PbGetMsg,
        MessageSvc.PushForceOffline,
        MessageSvc.PbSendMsg,
        FriendList.GetFriendGroupList,
        FriendList.GetTroopListSimplify,
        FriendList.GetTroopMemberList,
        ImgStore.GroupPicUp,
        LongConn.OffPicUp,
        LongConn.OffPicDown,
        TroopManagement.EditSpecialTitle,
        TroopManagement.Mute,
        TroopManagement.GroupOperation,
        TroopManagement.GetGroupOperationInfo,
        TroopManagement.EditGroupNametag,
        TroopManagement.Kick
    )

    object IncomingFactories : List<IncomingPacketFactory<*>> by mutableListOf(
        OnlinePush.PbPushGroupMsg,
        OnlinePush.ReqPush,
        OnlinePush.PbPushTransMsg,
        MessageSvc.PushNotify,
        ConfigPushSvc.PushReq

    )
    // SvcReqMSFLoginNotify 自己的其他设备上限
    // MessageSvc.PushReaded 电脑阅读了别人的消息, 告知手机
    // OnlinePush.PbC2CMsgSync 电脑发消息给别人, 同步给手机

    @Suppress("MemberVisibilityCanBePrivate") // debugging use
    fun findPacketFactory(commandName: String): PacketFactory<*>? {
        return OutgoingFactories.firstOrNull { it.receivingCommandName == commandName }
            ?: IncomingFactories.firstOrNull { it.receivingCommandName == commandName }
    }

    // 00 00 08 70 00 00 00 0A 01 00 00 00 00 0E 31 39 39 34 37 30 31 30 32 31 F5 45 50 03 B7 F7 99 8F DC CE F6 A2 35 08 04 85 D6 1D 52 90 24 13 86 A2 9D 40 D9 CD BF 4B 08 56 E7 EF 37 E6 E7 BE 09 FD A8 E5 D8 DA 07 90 E5 CB 22 1A 5A 77 88 B5 1A 0B 36 2A 71 59 1C 79 7C B1 84 39 CF 74 31 C5 ED 4D BB 12 F3 BB 10 B2 2E CD 05 82 86 AC 78 68 74 C3 42 9F CF F9 14 0D D4 2B 89 E9 FC 4D 1D 29 F3 08 A0 3A 62 F3 B5 71 D3 A9 06 BD 45 BA 50 D6 2D 68 05 B7 B4 D3 07 D0 01 A4 F4 71 0A 76 34 1F CA 2D 3A 53 6A C5 44 CA 56 C9 FA 0F 81 5E 06 63 45 9B BD FA 81 D3 50 46 74 34 C2 A8 21 9B 1B 25 E6 23 38 E5 87 B4 D2 A7 E7 59 65 03 BB 6D 46 D5 21 66 F6 2F 2D BF A3 77 2A 33 9D E1 35 18 45 CE 22 DB C1 52 BB 87 B9 F0 CB B1 CD 39 81 43 BD C1 E5 2B 81 DE 29 EC 72 2A 56 14 23 67 7B 60 F7 FB C1 7F 9C 18 8E ED 67 35 3E 2F EA D1 EA 24 AD 12 6D 65 B3 27 0C 2B 0A 43 42 3E 0C 81 C5 02 7D E6 A5 2C 79 8D 53 2D 06 F1 EE A5 51 BC 24 A6 F5 F2 0C BC 1F 42 F2 21 8F FB DF 9C 98 6F 25 FB D0 09 E1 F3 85 76 A2 96 39 5E 73 06 24 F8 1E 6C F7 D3 03 15 44 BA 6B 42 9E CE 14 7B 1A F9 0B A1 9A 85 FD 52 2B 34 FB 0F D8 6F 34 38 6E F3 A2 7D EB DF 3F 55 6A 52 27 35 44 5C 26 05 F3 36 8E 8F 00 D2 31 85 BF 20 8A 1C 37 57 C4 74 36 BF 89 3F 44 3C 97 AA 6B 8F D1 B6 CD D6 45 88 F1 74 35 CD 8B 85 9F 60 F4 B7 00 3E 87 10 C4 3F 7C 3F 22 F0 20 58 40 CE E6 77 8B 3D EE AA EB 31 52 65 BE C4 12 2B 28 65 1A 26 16 55 E9 86 28 90 42 FD 48 D6 6F E6 63 E2 40 3F BE 68 C6 FE A7 B2 10 31 15 7E B4 C7 2C 8D D9 04 8D AD 8E 45 D2 CA EC 07 D6 D0 14 FD 93 02 4B 3D 71 6E 3E 1F D5 BC ED 98 0E 01 6B FE 1C 96 24 34 DF A3 E3 EE 67 40 F3 C7 AA 10 4E 44 62 49 D5 45 14 15 49 04 2F 9F FC 5D 40 F7 5A CC C1 78 7B 37 35 80 F9 10 80 EB 40 DB 33 06 5F 2A 70 88 F0 94 49 59 48 4B 00 66 28 E9 E9 E7 CE E4 CA 6D 21 79 09 62 7A 6E C5 47 A4 08 85 73 43 35 2D EE E4 0E 85 47 92 B4 F1 5C 76 C2 72 BC D1 70 41 6D 9B EE F4 47 67 1F 63 57 B9 65 C1 08 5C 42 D6 28 FB 6F F5 1A 4B E0 B7 5A 36 75 E6 BE ED C6 67 DA 64 19 09 11 CF 9A 74 4A F4 4E 4F 1E 02 2C 8C 21 21 DE 76 15 34 E5 F2 DF F9 34 31 99 4F 4B B2 5E E6 2B 8D 7C 64 E9 7D 78 98 4F 28 FF 2E 95 1B 5D 8E 2F 70 E6 01 0A 82 18 90 16 8F 98 5E 0F CA 30 4F 36 AF 9B 5E 2B 5B FE 59 5E 11 BC A5 68 64 55 BD 04 CE 43 60 4C C9 CB F7 88 D2 30 7A 89 75 3D 93 9D A9 D0 CE 7D 4B CE 4D 14 5E 95 0E 18 3E 75 AE 20 C6 8A 6E 15 F9 39 58 3B 17 74 99 98 07 5D 68 B1 51 57 AF 1D 85 F1 A5 8A FE 9F 07 BC CB B9 84 A5 49 AE 9F F4 50 06 27 24 7F EB 66 C0 F7 C4 4F E9 74 FA 69 8E D0 A6 A4 3E 8A A8 D9 10 A1 05 03 0F B1 E7 80 22 A9 21 22 FA 31 A1 61 0E 5A 42 75 5F 19 95 2E FE A9 38 51 BB E9 C1 6B 29 CD AE 8F 78 7C 1F A4 81 F2 20 D3 15 C1 EA 42 BD 5F 52 C1 BA 5F C7 42 97 A8 F1 FA C6 20 82 3C 00 36 96 75 69 27 FE 93 AA BD 43 BD 0E FD ED 30 97 A6 B2 A2 E8 D3 75 C2 BF 35 EC 78 8A 3E 23 9D 6D DD 3C D4 4A BC 5A A8 74 86 3C A7 31 F5 F3 8A 78 9C 3C 75 88 B1 35 4A 83 91 EB 5F 80 F3 E0 0C EC 82 5C 7B 08 61 45 E3 3B 7B F1 1F BC 6D 5E 99 3A 34 68 28 20 42 8C 42 C9 0A 3F 32 42 03 E5 9A 71 BB E1 02 C9 B4 98 6E 73 7E EC 81 79 28 01 A8 E5 AC 0F 24 0A 58 DA C1 D2 F0 2C 94 E7 4D E5 ED B3 43 58 23 73 1D 7F 33 B0 7B 1A A8 C1 AC 6E 7C F1 A6 51 0F CF E9 51 5F 06 EB 79 C4 0C E9 78 6C B8 B8 11 C0 2E 84 BC F4 4D 05 D5 B5 90 20 30 BE 73 28 83 91 3B 4D 90 2E 8F A5 A8 BF 85 63 29 05 04 33 14 7D 24 24 7D 02 9B 39 5D 30 D0 68 58 57 1A 0D 67 A6 EF 44 4C 4E 92 14 0A 2A E4 24 1E 60 78 A3 65 71 7E E3 B2 44 E1 55 3E F2 08 05 F5 59 5E EE D3 0A 12 34 C2 FC 74 4A 8C F9 BE 7F 4D EC 9E D3 14 97 7F DF 75 E2 52 21 95 65 99 F4 1D 01 AD F4 D2 4D 8C 9E 5A 16 DE 65 AA D0 AC 24 17 FE BE B9 D8 9B FB 36 33 E5 AE 1B F9 B1 AE A4 9B 15 94 91 6D 45 BE 61 33 4B 71 40 BC DB D0 BF 06 39 83 D5 5F 30 6D E0 20 D3 33 AA 78 13 DA 9A 80 A3 38 4F 3B F8 AE B9 7F 9B B7 F1 AD 10 88 96 DD 79 DD F8 86 A3 72 A2 3F C2 6E B2 6D CE 36 54 90 5F DE 23 90 CD BD BD DB 95 83 5A 94 8E B8 54 AF 0A AB 10 0C 15 B3 3D 2B 89 7D 83 64 B9 29 08 50 40 6E F6 EC 8D E7 79 6C 2B 70 A5 07 FA AD 6A A1 54 EC D7 C7 70 A6 9F 8F 8E 5B 00 5E 8D 27 0D 21 8B 38 31 02 88 13 6E F4 5C 8D 15 82 FC 3E AE 57 17 3D 6D 38 F9 8C 2B EF 87 FA 30 FB 59 D4 E2 49 CF 76 75 9C 9D 50 8D 4A CD AE F6 3C 59 E8 92 BB C1 E2 5C F6 96 8B DC 78 DF E1 27 6B F1 1C D9 68 1D 29 8A E3 09 DD 28 C7 0A ED BB 75 6B D9 87 9F 7F 71 5D D0 EE 03 5B 28 52 40 26 DD C9 C4 52 4D 58 02 3A 71 BE 6C F4 6A 30 14 52 72 CB 45 95 80 DE 28 02 8A 22 7A 05 B5 D7 22 61 06 E0 34 09 AC A9 A8 A8 D0 DF 37 BE 3F 86 33 CA B0 EA 27 A5 95 A5 F8 1C A9 C5 46 41 EB DF C8 5F AD 85 7A 0E A8 AD A9 2A BF F3 6F 39 0D AE 60 30 B2 23 3C D0 37 85 18 01 2E 4E 74 0B 5D 98 2E 1E 3B 3F 30 DC CB DA 1E 0A 4F 0B 70 74 54 28 4F 82 6C 11 7E 5C 7E 33 C7 BB 47 29 2B F6 92 01 3F 83 AE 50 CC 6E B5 87 3B D4 F6 3A 6D 8D 0D 98 37 AB A6 08 79 EE E9 AB 38 37 FA 22 4C 54 80 1F 88 E9 E9 45 37 7A AF DB 61 A9 37 61 30 05 38 46 E4 CD ED B4 71 B2 C7 2E 1F 4C 92 27 C5 AF 3C D9 26 60 1F 4E 65 5E EF F7 A7 9C CC 49 F5 20 58 60 47 ED 44 DF EF FF AD 91 24 28 28 A4 03 F3 10 1B E5 18 0D 49 FA 68 A3 F2 BA 77 32 E4 16 35 66 8F 2C 8E 04 19 5A 26 AE 8C EB D1 C4 6F 0E 38 85 B7 3C 79 F4 98 29 35 FF DF 13 03 FE 36 D7 0A 20 3D 28 14 DE A1 D4 12 5B 6B D0 BF 53 4C AB 36 4E 3A FB 91 73 9C 39 F0 AB 10 1A 25 D6 D6 AC 2F 68 E5 33 67 23 EF 7A 97 70 2C F7 0B 67 0F A6 11 CB AB EF A9 3F 67 5B 8D 1B 39 C9 99 56 79 92 44 6E 1B 6F EE FE E9 08 9C 8B D6 5D 5A 8E 7B BD 61 A4 97 B2 78 75 98 5D C3 19 79 28 26 8B 00 32 D3 E1 3A 02 7F 12 D1 02 2C 5D 27 B3 F0 A6 8B 9D B1 01 01 EC AB 0F 6D D5 E4 0B B0 BD 54 9C 73 CD C0 AB F8 7E D4 D4 D3 3C E9 05 B3 30 30 3D C6 A4 70 EA 0B D1 47 CB 71 41 27 09 DA FE 4A 0F D2 1C 28 EF A6 3E 7A F6 05 A0 52 72 A3 C0 05 BC 8E 38 69 C5 16 AD 98 9D 96 4F FB 95 1D BD B7 12 26 AB 7D E2 EA 57 2C BB 1C 12 C9 FF 90 FB 6B 4F 3D FE 26 7D 4B FF A6 8D 4C D7 FA 4E ED B1 5F 5B 92 92 68 B5 25 0C BC 56 C7 AE 01 C4 05 9A A6 7E 97 93 4E 15 67 83 40 F7 45 8C F8 FB D8 C4 5A D0 94 15 AF 33 24 4A 4E 43 A6 B7 D4 AC 0C EB 39 C9 83 D3 E1 CE 36 6B AC CE FD 3C 92 2D 5D C2 6A 1C C4 54 21 4E 06 8B BB CB 7D B4 C2 FF 53 00 3C A4 47 98 AC 7E 29 AF B2 AB EB 25 1A 7E 3C E0 C6 DF 65 13 64 C1 94 EA 86 3F CD 54 E1 5E C5 95 3D F8 C0 A5 6B 28 CA 4F B4 5A E6 93 CE 49 5E AD CD A1 6E B8 94 9D C4 C3 0E 37 C4 10 A8 C0 47 17 C4 EE 3D 61 B5 80 ED EF 5B CC B4 49 69 10 4D E9 CD 22 B3 1B 1C 52 29 9D 6D B2 84 76 C3 CE 3C 23 39 0D 68 02 6B 1F 3F 28 DA 59 78 AE EA D8 F4 E0 1B 90 21 81 77 23 52 05 53 A1 BD E7 50 1C 24 26 9F 1D 39 E4 F2 A0 F8 7E F7 29 58 41 98 12 62 1A 23 B3 D9 A4 5C D3 15 0D 04 31 48 03 1B CC 5F A0 D1 A9 75 5C D0 FD 8A 9C FA 24 89 B2 C3 A9 C2 13 5B CD 1C F9 B1 63 C7 01 D7 BD 0D 43 2F CB 6C 4B 4F 0D
    // 00 00 08 E0 00 00 00 0A 01 00 00 00 00 0E 31 39 39 34 37 30 31 30 32 31 B4 16 A6 D7 A3 E4 9E 53 99 CD 77 14 70 1F 51 3E 8B 79 F6 93 2B E0 92 E4 32 E2 87 6C 3A 9C 1B 29 87 CB 3C 60 45 9C 41 71 63 6A F6 99 FC 05 01 68 86 B3 6F 37 97 52 C5 D3 0E 66 B3 F6 40 CC EB 18 A3 AE 15 3E 31 B1 E9 7C 6F EC E4 4D 31 F1 1E 2C 0C 1C 45 66 CD F7 1B 90 11 9A D8 CE DD 6D 6C 63 9F EB CD 69 33 AF 6C 8E BA 8C CB C3 FF 27 A2 A6 C3 28 06 4A B5 79 79 12 AB 52 04 62 CA 7D 11 59 85 5C 0B D6 8D 2A E7 9C 04 97 62 7D 05 11 3E 2C 11 60 E3 E3 B3 DA 7A 7C 13 AF 22 01 53 80 69 D0 F9 C8 86 EC 25 8C F3 67 5C 82 45 08 FB 34 43 50 01 0E EA 43 77 D8 CF EC 55 E6 4E 66 5B 26 21 C9 E8 78 92 AE 5C 61 F0 5E 0B E7 34 1F 53 D6 EA 28 9C 02 1A E9 F0 55 61 4B 06 F8 56 3B AC 93 B2 2C CD 66 0D D1 18 CB BD 29 50 DE 0F 82 6D 28 63 AB 21 E1 6C BA B1 9F 69 A4 E3 C9 20 F8 11 82 39 04 2B 54 44 50 FA 2E 86 68 6D DC 5D 9E 18 F4 DD 19 09 BC CF E8 41 68 A3 8D 86 42 80 51 C4 C1 ED 54 DB 50 F5 1D A7 28 2A 0D E8 14 1A 4E F7 96 29 00 6C 9D 4A 2E 3E 7B 4C AC 20 78 F1 3C 70 6B 61 96 D7 EC 77 AD CB AD AF BB 47 C3 1F A0 6C 6C 9C 9F F3 6C EB 6C A4 D0 7F 2B E1 AA 68 26 99 B9 C8 A1 F5 C4 7E E7 E7 81 EE 66 00 96 33 49 C0 EE A2 F9 F6 52 C5 A6 5D EE 9D C5 E5 CE DA 31 FC FF 4B 02 97 68 3D 6A 99 4A CF 69 D9 F4 53 68 31 E7 32 2F 85 E7 7F 16 82 AE FA 73 D5 42 09 9C CB 53 26 79 41 63 80 B0 E2 6A 8B B9 C6 71 08 B4 2B E0 48 D3 C4 0F B0 00 D0 FA 8C 29 DE E9 71 6A D7 89 76 E7 5D 33 14 10 6F E2 44 6A A0 DC C1 CB F3 9A C3 13 CB D1 82 2C DF 34 68 79 E3 09 BD CC 2B 25 79 A8 E7 BE 29 6C 97 C3 D7 F4 0E CC 2B 74 71 02 BA 2B 5B 57 1B C2 C8 C2 BF 54 23 72 EA E4 38 54 20 7D 88 E4 39 7C C5 8A 1B C0 EC D2 1E 7D 1B 6B 7A BC EC 73 1E 53 4A 6F 4F EA F0 56 12 80 BD 0B 37 67 BD FD A8 29 23 2D 8E 66 7E 31 A9 F6 CE 7E BC 4F 38 D0 33 D4 C7 4A E9 43 9D 28 2E 8F 7C D5 81 F4 8C F9 6F 21 AC A1 08 FD F4 01 FB E8 CE 61 91 BE 68 5B E4 3A 5F F8 FB DA 5D 9B 2A AF E2 0C D3 A4 1F 42 90 96 E1 28 44 85 8D E1 CF 19 A9 47 04 8D 28 D9 B3 35 79 48 70 D9 ED 45 B6 24 B5 56 FA 1E DE 02 F3 EB 69 08 7D 24 9C 60 35 97 8D 13 4A 5A 57 BA B3 14 C1 EE 70 22 CA B2 65 F7 BB 3F A2 D9 14 AA 4C 52 E6 E4 10 D3 FD C6 2B DD BF C0 CF E5 35 57 9E 9F D0 77 C8 E6 EF 2B 8E 01 88 96 F8 68 95 A7 0D 58 81 30 60 88 44 CC 31 5B C1 D4 92 6E ED 17 CA 0A 01 69 90 4E 6A C0 D7 09 6C E5 33 64 CA 6E 5C 07 C3 AD 46 36 F9 DF DE B7 71 B2 87 CB 3D 76 C0 44 B8 6B 15 27 B2 03 99 C7 51 8A 00 35 C9 1C 76 55 32 AE 49 5A 34 6A 4E FD 20 7A 24 BF 34 E8 B4 18 BC 92 64 A1 F3 0A 2E 7B 00 EA B6 52 E7 AC 34 FD AE FF 1E 5D 6D D6 1F 6D 06 31 09 9D A9 9C 86 DB 5E 05 07 BA 4A 49 2B D2 7F EE 88 64 B2 6F 15 70 39 1B E9 57 6A 4E 29 4A A4 57 EA 80 3D 86 4C E9 F7 F5 2B C4 9F 35 62 76 09 0E 1C A4 99 50 99 82 2F 84 90 0E 9E 9F 75 C3 15 B0 61 34 D1 67 2D 30 16 FE D3 BF 59 6A B1 74 02 C4 EF 92 85 E0 16 4B 0C C5 9D 65 BB 5D 52 8F 52 5B 7C 7B 74 D9 EC 41 A9 5B FA 2D 95 D4 AE 5D F1 68 88 F6 82 ED 09 05 21 2E 5D 93 64 A0 96 15 64 A6 50 3C 03 2B FC 3E 80 89 90 62 CC D9 23 8E D7 BD 05 02 30 86 32 31 6A 5F F8 C4 BD 61 D0 CE B9 54 4E 93 E9 AE B9 4F 2B 98 DC 23 31 CC A8 06 89 A8 08 60 99 DC D4 81 98 13 C9 27 36 32 24 C1 B0 6B F0 3D EB CC 3B 32 5F 20 72 23 B3 DF 0B 48 3C 35 FD F1 FB DC 3E 2A BE B9 0F 42 56 F1 39 94 86 85 C6 1E A0 4C EC B8 69 45 5F 3D AB 3C 3B A2 70 61 91 9D 2C DD 6D C5 E9 EF 47 36 A6 A3 E0 96 C2 B8 EF 92 E9 E0 26 88 C6 B5 51 BA DE FD C5 BA 4C 6A 9A FE 6F DE B8 10 05 7F 9C 5D 40 11 39 75 CD 36 4F 6B A8 A1 94 57 5F 8F F2 D0 E2 36 A0 A4 24 05 FD 9E F5 51 93 C9 6E 5A 10 8D C3 33 2D E5 09 7A E0 DB 44 63 9C EA A5 ED BF 0B 98 32 F1 BA 04 96 F6 14 49 F1 F8 58 EA 6E 5E 5E 49 CA 2D E2 93 E6 AD 20 B2 CD 98 A7 3E BA 3E A8


    /**
     * full packet without length
     */
    // do not inline. Exceptions thrown will not be reported correctly
    @UseExperimental(MiraiInternalAPI::class)
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Packet> parseIncomingPacket(bot: QQAndroidBot, rawInput: Input, consumer: PacketConsumer<T>) = with(rawInput) {
        // login
        val flag1 = readInt()

        PacketLogger.verbose("开始处理一个包")
        PacketLogger.verbose("flag1(0A/0B) = ${flag1.toUByte().toUHexString()}")
        // 00 00 05 30
        // 00 00 00 0A // flag 1
        // 01 // packet type. 02: sso, 01: uni
        //
        // 00 00 00 00 0E 31 39 39 34 37 30 31 30 32 31 40 3C 63 DC A2 8F FC E7 09 66 62 11 A3 5A B6 AB DC 6E A1 CA CF E2 0A 6F A8 6D 36 64 4E 22 4B A9 8A ED 07 7A 0A 9E F3 C7 7B 72 EF C1 C7 6E 9A 28 27 10 F8 2A 7F 37 49 B6 48 35 52 E9 CF 2A B5 F3 26 90 33 68 9B 5A 04 2A 8B F5 78 13 82 FE 3C 13 C4 F9 38 39 0E 02 4C 3D 91 0A 2A 94 3F 9F A6 52 B9 14 89 C5 D9 57 0F 96 F8 0E 7D 32 81 8E 10 DB C0 CA BE C7 3F EC D0 B1 F0 9D A2 4B 9F B3 8D E0 EB 1F 42 52 EA 5E 9E 76 E2 F4 13 9D 0E 7E 6D 0A E3 56 C3 EE 8A 80 24 DE FB 08 82 FB B7 AF CE 2A 69 16 E3 C3 79 5C C7 CD 44 BA AA 08 A2 51 0B 43 31 69 A1 12 D1 AE 48 15 AE 76 E9 AB BB D2 E0 16 03 EB 2D 47 A4 61 24 65 5E CC C5 03 B3 96 3E 7A 39 90 3D DB 63 56 2B 23 85 CE 5F 9E 04 20 45 31 79 7B BF 78 33 77 34 C1 8E 83 B3 50 88 2A 01 C0 C4 E4 BF 2D 0D B9 37 32 AB E0 BB 82 36 B1 4E 51 4B F7 07 6A 12 3E 79 EA 93 3D BD 06 4E AE 1C 49 82 17 14 00 09 59 40 A6 A9 01 56 1A 23 86 A8 33 B3 9A 70 7B 3A C1 F9 31 03 FD DB 4B 5C 7B F9 BB 43 94 65 A0 1C DA 2B 85 AA AD 7B 79 42 F2 EB 25 5E F0 DA B7 E7 AD 4B 25 02 36 BB 78 5F 83 7F F7 78 F0 99 D2 B5 A3 0C 4A 7F 0E B0 A6 C4 99 F7 9E 0B C6 4D FC F5 8D 6B 5F 35 27 36 D3 DB D0 46 C7 10 76 7D 96 91 48 EA 1C B2 B7 D7 2F D2 88 A8 4C 87 D6 A9 40 33 4C 76 C5 48 3E 32 4D C1 C3 7F 5C D9 B3 22 00 88 BE 04 82 64 A9 73 AA E1 65 1A EF 49 B4 54 74 53 FF 75 B6 E9 57 1B 89 2D 6F 2A 6A CE 23 BF 41 CB 55 B3 A0 53 87 AD A0 22 EE 6B 3F 4A 97 23 36 BF 7E 08 2D 0A 9E 2E 4B F2 2E 00 59 EC F1 21 34 45 75 DB 6B F2 EC 65 24 30 69 50 CC 45 78 00 AF C8 F6 3D 8E 03 60 CF CA A1 88 14 18 82 6F 56 58 D0 BC E0 48 FD AA 86 63 CA C1 01 63 07 16 4A 79 79 17 9D 1F E2 40 4B B6 77 6E 44 84 DE BE 02 4C 33 7A F5 2F 93 21 3E 17 62 38 81 95 E6 84 8B 7C C8 7B E2 23 FB 12 4F E8 42 5F 1D 48 92 84 B1 45 FF 69 97 3C 30 C9 09 E8 84 E8 07 0E 17 D4 A1 CC 35 D6 FE 7B D2 9A 44 8B 17 BF E7 D6 98 1D 98 D7 30 BE 55 19 A9 F4 D6 0D E8 18 80 35 85 B6 AB B9 20 32 C7 ED C6 AD A7 AE 19 48 B7 17 02 B3 45 C3 A2 B9 C9 B7 58 B5 8B 4C AF 52 AD A1 E1 62 45 AB 58 26 67 20 C7 64 AA DA 7E F3 70 8B C2 92 69 E3 3E 3E 6F 39 6F 2B 35 35 0F 00 FC 52 B5 5C 5B 73 FE F6 F5 10 55 36 7C 9A 84 FC A6 23 29 4A 75 49 7C 13 1C CA 54 A2 A2 FA 2A 63 A5 4C 9A B4 27 E8 5F 9F 23 96 B2 E7 AA E6 8B E0 E2 6A 75 8A B2 F4 E4 7E 09 E8 22 70 2A 42 8B E3 DC AD E8 A8 A2 92 71 6B A2 12 78 E1 DA CC 70 57 67 F5 B4 52 F3 B4 4C 17 AB 05 33 DA 6E 47 52 C5 B2 B7 9A D2 A8 BC 44 64 D3 26 1A 6B C6 C5 36 1C 2B 8F BD B7 27 91 3E C0 C2 FC 03 41 FE 02 D3 4B B1 E5 5F 5B 50 05 29 BD 3A 64 85 E3 8C FB 11 F2 1D 94 DB D7 78 AF AD 77 A3 9C D4 39 5D 8B EA DF 9D 08 CA 92 7C 5F D5 17 49 0E FA A1 21 1C 9F C3 88 1A DC E7 D8 82 80 85 86 32 99 15 E4 89 BA 91 2B 4B FB 87 EC 44 B4 D9 83 CC 79 77 A4 A0 D0 50 E3 4F 00 E7 DA DA 79 38 1E D8 04 86 16 CD 25 BE BA 76 E4 8C F9 86 91 69 6E C7 A0 EF 6B 44 2B C9 C3 DC 8D 2D 65 60 7A F4 37 02 D4 8F 38 D0 D5 20 30 DE A5 F5 A8 75 C7 EE 0B 0F 1B 88 C2 8A CC 6F 70 1D E4 D8 4E DD 04 A5 5B B8 04 B1 29 42 08 92 19 78 E2 26 EB 6B 07 49 DE 8A AF A3 41 72 1D E2 3C 62 0F 7E 7B DE A3 0F 71 8C 5D EC E9 96 96 45 A9 39 33 8A 87 C9 93 CE 3B 6D 75 50 21 1F 4C 03 E9 A7 AD 03 0F 5E A9 EE 60 CC EA 05 4F DF E1 B1 13 A6 7D C7 B9 37 58 53 3B 06 1A AD 98 E5 06 D9 74 2A B1 96 75 DE A6 B7 89 25 53 2A A3 07 B6 70 C6 86 1F 59 EB 53 08 57 6E 86 D7 A1 5C DB 26 D7 86 3E 97 BB FD 6A 0A 4C E1 81 B9 4C C1 A0 49 89 57 29 E0 CD 79 6F 0A 46 C1 C6 62 75 49 C6 9A B9 22 75 EE 10 C7 56 E6 D5 DE 4D EC 89 5A 6F AC 60 0F B3 CC 37 9E F2 BE 49 A7 77 3C 05 AE 92 66 C8 BE 16 E5 35 17 24 18 A5 CE B8 BB AE CD 88 DE 01 53 40 84 E0 06 C6 77 96 09 DF D7 76 3B CA C9 B5 B2 91 95 07 54 6F 51 EB 12 58 16 8A AF C3 E3 B9 4A EC 25 A5 D1 19 59 72 F5 E3 4F 7C 40 B2 D0 4E 9F 50 13 FB 86 C3 6A 88 32 5B 67 EC 4F 0E 0B 31 F8 0C 02 6C CE 8D 50 55 A2 B3 57 73 7C 78 D3 43 1F 48 33 51 E7 0A D0 6D 46 71 4A AD 66 50 F9 96 11 4F A5 5B 3C A0 3E 46 D2 CB 3B A1 03 84 9C 8E 4E 2D 83 69 2E 17 9B F8 36 63 F1 93 CA F9 32 57 2B AB 4E 14 A3 5A F1 39 B0 3F 0F 99 CC 9B FB 7E BC 0A AA C9 65 3C C8 B4 B0 1F
        val flag2 = readByte().toInt()
        PacketLogger.verbose("包类型(flag2) = $flag2. (可能是 ${if (flag2 == 2) "OicqRequest" else "Uni"})")

        val flag3 = readByte().toInt()
        check(flag3 == 0) { "Illegal flag3. Expected 0, whereas got $flag3. flag1=$flag1, flag2=$flag2. Remaining=${this.readBytes().toUHexString()}" }

        readString(readInt() - 4)// uinAccount

        //debugPrint("remaining")

        ByteArrayPool.useInstance { data ->
            val size = this.readAvailable(data)

            kotlin.runCatching {
                // 快速解密
                if (flag2 == 2) {
                    PacketLogger.verbose("SSO, 尝试使用 16 zero 解密.")
                    data.decryptBy(DECRYPTER_16_ZERO, size).also { PacketLogger.verbose("成功使用 16 zero 解密") }
                } else {
                    PacketLogger.verbose("Uni, 尝试使用 d2Key 解密.")
                    data.decryptBy(bot.client.wLoginSigInfo.d2Key, size).also { PacketLogger.verbose("成功使用 d2Key 解密") }
                }
            }.getOrElse {
                // 慢速解密
                PacketLogger.verbose("失败, 尝试其他各种key")
                bot.client.tryDecryptOrNull(data, size) { it }
            }?.toReadPacket()?.let { decryptedData ->
                // 解析外层包装
                when (flag1) {
                    0x0A -> parseSsoFrame(bot, decryptedData)
                    0x0B -> parseSsoFrame(bot, decryptedData) // 这里可能是 uni?? 但测试时候发现结构跟 sso 一样.
                    else -> error("unknown flag1: ${flag1.toByte().toUHexString()}")
                }
            }?.let {
                // 处理内层真实的包
                if (it.packetFactory == null) {
                    PacketLogger.warning("找不到 PacketFactory")
                    PacketLogger.verbose("传递给 PacketFactory 的数据 = ${it.data.useBytes { data, length -> data.toUHexString(length = length) }}")
                    return
                }

                it.data.withUse {
                    when (flag2) {
                        1 ->//it.data.parseUniResponse(bot, it.packetFactory, it.sequenceId, consumer)
                            when (it.packetFactory) {
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

                        // for oicq response, factory is always OutgoingPacketFactory
                        2 -> it.data.parseOicqResponse(bot, it.packetFactory as OutgoingPacketFactory<T>, it.sequenceId, consumer)
                        else -> error("unknown flag2: $flag2. Body to be parsed for inner packet=${it.data.readBytes().toUHexString()}")
                    }
                }
            } ?: inline {
                // 无法解析
                PacketLogger.error("任何key都无法解密: ${data.take(size).toUHexString()}")
                return
            }
        }
    }

    private inline fun <R> inline(block: () -> R): R = block()

    class IncomingPacket(
        val packetFactory: PacketFactory<*>?,
        val sequenceId: Int,
        val data: ByteReadPacket
    )

    /**
     * 解析 SSO 层包装
     */
    @UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
    private fun parseSsoFrame(bot: QQAndroidBot, input: ByteReadPacket): IncomingPacket {
        val commandName: String
        val ssoSequenceId: Int
        val dataCompressed: Int
        // head
        input.readPacket(input.readInt() - 4).withUse {
            ssoSequenceId = readInt()
            PacketLogger.verbose("sequenceId = $ssoSequenceId")
            val returnCode = readInt()
            if (returnCode != 0) {
                error("returnCode = $returnCode")
            }
            val extraData = readBytes(readInt() - 4)
            PacketLogger.verbose("(sso/inner)extraData = ${extraData.toUHexString()}")

            commandName = readString(readInt() - 4)
            bot.client.outgoingPacketUnknownValue = readBytes(readInt() - 4)

            if (commandName == "ConfigPushSvc.PushReq") {
                bot.client.configPushSvcPushReqSequenceId = ssoSequenceId
            }
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
                    data.unzip(length = length).let {
                        val size = it.toInt()
                        if (size == it.size || size == it.size + 4) {
                            it.toReadPacket(offset = 4)
                        } else {
                            it.toReadPacket()
                        }
                    }
                }
            }
            else -> error("unknown dataCompressed flag: $dataCompressed")
        }

        // body
        val packetFactory = findPacketFactory(commandName)

        bot.logger.debug("Received commandName: $commandName")
        return IncomingPacket(packetFactory, ssoSequenceId, packet)
    }

    private suspend fun <T : Packet> ByteReadPacket.parseOicqResponse(
        bot: QQAndroidBot,
        packetFactory: OutgoingPacketFactory<T>,
        ssoSequenceId: Int,
        consumer: PacketConsumer<T>
    ) {
        check(readByte().toInt() == 2)
        this.discardExact(2) // 27 + 2 + body.size
        this.discardExact(2) // const, =8001
        this.readUShort() // commandId
        this.readShort() // const, =0x0001
        this.readUInt().toLong() // qq
        val encryptionMethod = this.readUShort().toInt()

        this.discardExact(1) // const = 0
        val packet = when (encryptionMethod) {
            4 -> { // peer public key, ECDH
                var data = this.decryptBy(bot.client.ecdh.keyPair.initialShareKey, (this.remaining - 1).toInt())

                val peerShareKey = bot.client.ecdh.calculateShareKeyByPeerPublicKey(readUShortLVByteArray().adjustToPublicKey())
                data = data.decryptBy(peerShareKey)

                packetFactory.decode(bot, data)
            }
            0 -> {
                val data = if (bot.client.loginState == 0) {
                    ByteArrayPool.useInstance { byteArrayBuffer ->
                        val size = (this.remaining - 1).toInt()
                        this.readFully(byteArrayBuffer, 0, size)

                        runCatching {
                            byteArrayBuffer.decryptBy(bot.client.ecdh.keyPair.initialShareKey, size)
                        }.getOrElse {
                            byteArrayBuffer.decryptBy(bot.client.randomKey, size)
                        }.toReadPacket() // 这里实际上应该用 privateKey(另一个random出来的key)
                    }
                } else {
                    this.decryptBy(bot.client.randomKey, 0, (this.remaining - 1).toInt())
                }

                packetFactory.decode(bot, data)

            }
            else -> error("Illegal encryption method. expected 0 or 4, got $encryptionMethod")
        }

        consumer(packetFactory, packet, packetFactory.commandName, ssoSequenceId)
    }

}

@UseExperimental(ExperimentalContracts::class)
internal inline fun <R> IoBuffer.withUse(block: IoBuffer.() -> R): R {
    contract {
        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block(this)
    } finally {
        this.release(IoBuffer.Pool)
    }
}

@UseExperimental(ExperimentalContracts::class)
internal inline fun <R> ByteReadPacket.withUse(block: ByteReadPacket.() -> R): R {
    contract {
        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block(this)
    } finally {
        this.close()
    }
}