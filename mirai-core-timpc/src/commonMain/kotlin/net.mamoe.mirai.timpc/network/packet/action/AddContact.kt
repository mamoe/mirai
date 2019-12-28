@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.action

import kotlinx.io.core.*
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.utils.PacketVersion

import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.io.*


/**
 * 查询某人与机器人账号有关的曾用名 (备注).
 *
 * 曾用名可能是:
 * - 昵称
 * - 共同群内的群名片
 */
@PacketVersion(date = "2019.11.11", timVersion = "2.3.2 (21173)")
internal object QueryPreviousNamePacket : SessionPacketFactory<PreviousNameList>() {
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey,
        target: Long
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey) {
        writeZero(2)
        writeQQ(bot)
        writeQQ(target)
    }

    // 01BC 曾用名查询. 查到的是这个人的
    // 发送  00 00
    //       3E 03 3F A2 //bot
    //       59 17 3E 05 //目标
    //
    // 接受: 00 00 00 03
    //      [00 00 00 0C] E6 A5 BC E4 B8 8A E5 B0 8F E7 99 BD
    //      [00 00 00 10] 68 69 6D 31 38 38 E7 9A 84 E5 B0 8F 64 69 63 6B
    //      [00 00 00 0F] E4 B8 B6 E6 9A 97 E8 A3 94 E5 89 91 E9 AD 94

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): PreviousNameList {
        // 00 00 00 01 00 00 00 0F E8 87 AA E5 8A A8 E9 A9 BE E9 A9 B6 31 2E 33

        val count = readUInt().toInt()
        return PreviousNameList(ArrayList<String>(count).apply {
            repeat(count) {
                discardExact(2)
                add(readUShortLVString())
            }
        })
    }
}

// 需要验证消息
// 0065 发送 03 07 57 37 E8
// 0065 接受 03 07 57 37 E8 10 40 00 00 10 14 20 00 00 00 00 00 00 00 01 00 00 00 00 00

/**
 * 向服务器检查是否可添加某人为好友
 *
 * @author Him188moe
 */
@PacketVersion(date = "2019.11.11", timVersion = "2.3.2 (21173)")
internal object CanAddFriendPacket : SessionPacketFactory<CanAddFriendResponse>() {
    operator fun invoke(
        bot: Long,
        qq: Long,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey) {
        writeQQ(qq)
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): CanAddFriendResponse =
        with(handler.bot) {
            if (remaining > 20) {//todo check
                return CanAddFriendResponse.AlreadyAdded(readQQ().qq())
            }
            val qq: QQ = readQQ().qq()

            readUByteLVByteArray()
            // debugDiscardExact(1)

            return when (val state = readUByte().toUInt()) {
                //09 4E A4 B1 00 03
                0x00u -> CanAddFriendResponse.ReadyToAdd(qq)

                0x01u -> CanAddFriendResponse.RequireVerification(qq)
                0x99u -> CanAddFriendResponse.AlreadyAdded(qq)

                0x03u,
                0x04u -> CanAddFriendResponse.Rejected(qq)
                else -> error(state.toString())
            }
        }

}

internal sealed class CanAddFriendResponse : EventPacket {
    abstract val qq: QQ

    /**
     * 已经添加
     */
    data class AlreadyAdded(
        override val qq: QQ
    ) : CanAddFriendResponse()

    /**
     * 需要验证信息
     */
    data class RequireVerification(
        override val qq: QQ
    ) : CanAddFriendResponse()

    /**
     * 不需要验证信息
     */
    data class ReadyToAdd(
        override val qq: QQ
    ) : CanAddFriendResponse()

    /**
     * 对方拒绝添加
     */
    data class Rejected(
        override val qq: QQ
    ) : CanAddFriendResponse()
}

/*
包ID 0115, 在点击提交好友申请时
发出 03 5D 12 93 30
接受 03 00 00 00 00 01 30 5D 12 93 30 00 14 00 00 00 00 10 30 36 35 39 E4 B8 80 E7 BE 8E E5 A4 A9 E9 9D 99 02 0A 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 1E
 */

internal inline class FriendAdditionKey(val value: IoBuffer)

/**
 * 请求一个 32 位 Key, 在添加好友时发出
 */
@PacketVersion(date = "2019.11.11", timVersion = "2.3.2 (21173)")
internal object RequestFriendAdditionKeyPacket : SessionPacketFactory<RequestFriendAdditionKeyPacket.Response>() {
    operator fun invoke(
        bot: Long,
        qq: Long,
        sessionKey: SessionKey
    ) = buildSessionPacket(bot, sessionKey) {
        //01 00 01 02 B3 74 F6
        writeHex("01 00 01")
        writeQQ(qq)
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): Response {
        //01 00 01 00 00 20 01 C2 76 47 98 38 A1 FF AB 64 04 A9 81 1F CC 2B 2B A6 29 FC 97 80 A6 90 2D 26 C8 37 EE 1D 8A FA
        discardExact(4)
        return Response(FriendAdditionKey(readIoBuffer(readUShort().toInt())))
    }

    data class Response(
        val key: FriendAdditionKey
    ) : Packet
}

/**
 * 请求添加好友
 */
internal object AddFriendPacket : SessionPacketFactory<AddFriendPacket.Response>() {
    @PacketVersion(date = "2019.11.11", timVersion = "2.3.2 (21173)")
    @Suppress("FunctionName")
    fun RequestAdd(
        bot: Long,
        qq: Long,
        sessionKey: SessionKey,
        /**
         * 验证消息
         */
        message: String?,
        /**
         * 备注名
         */
        remark: String?, //// TODO: 2019/11/15 无备注的情况
        key: FriendAdditionKey
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey, name = "AddFriendPacket.RequestAdd") {

        //02 5D 12 93 30
        // 00
        // 00 [00 20] 3C 00 0C 44 17 C2 15 99 F9 94 96 DC 1C D5 E3 45 41 4B DB C5 B6 B6 52 85 14 D5 89 D2 06 72 BC C3
        // 01 [00 1E] E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A
        // 00 2A 00 01 00 01 00 00 00 1B E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A 00 05 00 00 00 00 01 00


        //02 02 B3 74 F6
        // 00 00
        // [00 20] 06 51 61 A0 CE 33 FE 3E B1 32 41 AF 9A F0 EB FD 16 D5 3A 71 89 3A A4 5C 00 0F C4 57 31 A3 35 76
        // 01 00 00 00 0F 00 01 00 01 00 00 00 00 00 05 00 00 00 00 01 00

        //02 02 B3 74 F6
        // 00
        // 00 [00 20] 01 C2 76 47 98 38 A1 FF AB 64 04 A9 81 1F CC 2B 2B A6 29 FC 97 80 A6 90 2D 26 C8 37 EE 1D 8A FA
        // 01 [00 00]
        // 00 0F 00 01 00 01 00 00 00 00 00 05 00 00 00 00 01 00
        writeUByte(0x02u)
        writeQQ(qq)
        writeByte(0)
        writeByte(0); writeShort(key.value.readRemaining.toShort()); writeFully(key.value)
        writeByte(1); writeShortLVString(message ?: "")
        writeShortLVPacket {
            //00 01 00 01 00 00 00 1B E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A E5 95 8A 00 05 00 00 00 00 01

            //00 01 00 01 00 00 00 00 00 05 00 00 00 00 01
            writeHex("00 01 00 01 00 00")// TODO: 2019/11/11 这里面或者下面那个hex可能包含分组信息. 这两次测试都是用的默认分组即我的好友
            writeShortLVString(remark ?: "")
            writeHex("00 05 00 00 00 00 01")
        }
        writeByte(0)
        //  write
    }

    // 03 76 E4 B8 DD
    // 00 00 09 //分组
    // 00 29 //有备注
    // 00 09 00 02 00 00 00 00
    // [00 18] E8 87 AA E5 8A A8 E9 A9 BE E9 A9 B6 31 2E 33 E5 93 88 E5 93 88 E5 93 88
    // [00 05] 00 00 00 00 01

    // 03 76 E4 B8 DD
    // 00 00 09 00 11 00 09 00 02 00 00 00 00 //没有备注, 选择分组和上面那个一样
    // 00 00 00 05 00 00 00 00 01

    // 03 76 E4 B8 DD
    // 00 00 00
    // 00 11 //没有备注
    // 00 09 00 02 00 00 00 00
    // 00 00 00 05 00 00 00 00 01
    @Suppress("FunctionName")
    @PacketVersion(date = "2019.11.20", timVersion = "2.3.2 (21173)")
    fun Approve(
        bot: Long,
        sessionKey: SessionKey,
        /**
         * 好友列表分组的组的 ID. "我的好友" 为 0
         */
        friendListId: Short,
        qq: Long,
        /**
         * 备注. 不设置则需要为 `null` TODO 需要确认是否还需发送一个设置备注包. 因为测试时若有备注则会多发一个包并且包里面有所设置的备注
         */
        remark: String?
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey, version = TIMProtocol.version0x02, name = "AddFriendPacket.Approve") {
        writeByte(0x03)
        writeQQ(qq)
        writeZero(1)
        writeUShort(friendListId.toUShort())
        writeZero(1)
        when (remark) {
            null -> writeUByte(0x11u)
            else -> writeUByte(0x29u)
        }
        writeHex("00 09 00 02 00 00 00 00")
        when (remark) {
            null -> writeZero(2)
            else -> writeShortLVString(remark)
        }
        writeHex("00 05 00 00 00 00 01")
    }

    internal object Response : Packet {
        override fun toString(): String = "AddFriendPacket.Response"
    }


    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): Response {
        //02 02 B3 74 F6 00 //02 B3 74 F6 是QQ号
        return Response
    }
}
