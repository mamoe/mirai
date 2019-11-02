@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUInt
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.NullMessageChain
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.printTLVMap
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.readTLVMap
import net.mamoe.mirai.utils.io.readUShortLVByteArray
import kotlin.properties.Delegates


enum class SenderPermission {
    OWNER,
    OPERATOR,
    MEMBER;
}

@Suppress("EXPERIMENTAL_API_USAGE")
@PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
class GroupMessageEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) :
    ServerEventPacket(input, eventIdentity) {
    var groupNumber: UInt by Delegates.notNull()
    var qq: UInt by Delegates.notNull()
    lateinit var senderName: String
    /**
     * 发送方权限.
     */
    lateinit var senderPermission: SenderPermission
    var message: MessageChain = NullMessageChain

    override fun decode() = with(input) {
        discardExact(31)
        groupNumber = readUInt()
        discardExact(1)
        qq = readUInt()

        discardExact(48)
        readUShortLVByteArray()
        discardExact(2)//2个0x00
        message = readMessageChain()

        val map = readTLVMap(true)
        if (map.containsKey(18u)) {
            map.getValue(18u).read {
                val tlv = readTLVMap(true)
                //tlv.printTLVMap("消息结尾 tag=18 的 TLV")
                ////群主的18: 05 00 04 00 00 00 03 08 00 04 00 00 00 04 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08
                //群主的 子map= {5=00 00 00 03, 8=00 00 00 04, 1=48 69 6D 31 38 38 6D 6F 65, 3=04, 4=00 00 00 08}
                //管理员 子map= {5=00 00 00 03, 8=00 00 00 04, 2=65 6F 6D 38 38 31 6D 69 48, 3=02, 4=00 00 00 10}
                //群成员 子map= {5=00 00 00 03, 8=00 00 00 04, 2=65 6F 6D 38 38 31 6D 69 48, 3=02}

                // 4=08, 群主
                // 没有4, 群员
                // 4=10, 管理员

                senderPermission = when (tlv.takeIf { it.containsKey(0x04u) }?.get(0x04u)?.getOrNull(3)?.toUInt()) {
                    null -> SenderPermission.MEMBER
                    0x08u -> SenderPermission.OWNER
                    0x10u -> SenderPermission.OPERATOR
                    else -> {
                        tlv.printTLVMap("TLV(tag=18) Map")
                        MiraiLogger.warning("Could not determine member permission, default permission MEMBER is being used")
                        SenderPermission.MEMBER
                    }
                }

                senderName = when {
                    tlv.containsKey(0x01u) -> kotlinx.io.core.String(tlv.getValue(0x01u))//这个人的qq昵称
                    tlv.containsKey(0x02u) -> kotlinx.io.core.String(tlv.getValue(0x02u))//这个人的群名片
                    else -> {
                        tlv.printTLVMap("TLV(tag=18) Map")
                        MiraiLogger.warning("Could not determine senderName")
                        "null"
                    }
                }
            }
        }
    }
}

//
//以前的消息: 00 00 00 25 00 08 00 02 00 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 76 E4 B8 DD 58 2C 60 86 35 3A 30 B3 C7 63 4A 80 E7 CD 5B 64 00 0B 78 16 5D A3 0A FD 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D A3 0A FD AB 77 16 02 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 04 01 00 01 36 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00
//刚刚的消息: 00 00 00 2D 00 05 00 02 00 01 00 06 00 04 00 01 2E 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 76 E4 B8 DD 11 F4 B2 F2 1A E7 1F C4 F1 3F 23 FB 74 80 42 64 00 0B 78 1A 5D A3 26 C1 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D A3 26 C1 AA 34 08 42 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E4 BD A0 E5 A5 BD 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00

class FriendMessageEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) :
    ServerEventPacket(input, eventIdentity) {
    val qq: UInt get() = eventIdentity.from

    /**
     * 是否是在这次登录之前的消息, 即消息记录
     */
    var isPrevious: Boolean = false

    var message: MessageChain by Delegates.notNull()

    //来自自己发送给自己
    //00 00 00 20 00 05 00 02 00 06 00 06 00 04 00 01 01 07 00 09 00 06 03 E9 20 02 EB 94 00 0A 00 04 01 00 00 00 0C 17 76 E4 B8 DD 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B A6 D2 5D A3 2A 3F 00 00 5D A3 2A 3F 01 00 00 00 00 4D 53 47 00 00 00 00 00 5D A3 2A 3F 0C 8A 59 3D 00 00 00 00 0A 00 86 02 00 06 E5 AE 8B E4 BD 93 00 00 01 00 06 01 00 03 31 32 33 19 00 1F 01 00 1C AA 02 19 08 00 88 01 00 9A 01 11 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 0E 00 0E 01 00 04 00 00 00 00 0A 00 04 00 00 00 00

    override fun decode() = with(input) {
        input.discardExact(2)
        val l1 = readShort()
        discardExact(1)//0x00
        isPrevious = readByte().toInt() == 0x08
        discardExact(l1.toInt() - 2)
        //java.io.EOFException: Only 49 bytes were discarded of 69 requested
        //抖动窗口消息
        discardExact(69)
        readUShortLVByteArray()//font
        discardExact(2)//2个0x00
        message = readMessageChain()

        //val map: Map<Int, ByteArray> = readTLVMap(true).withDefault { byteArrayOf() }
        //map.printTLVMap("readTLVMap")
        //println("map.getValue(18)=" + map.getValue(18).toUHexString())

        //19 00 38 01 00 35 AA 02 32 50 03 60 00 68 00 9A 01 29 08 09 20 BF 02 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 B8 03 00 C0 03 00 D0 03 00 E8 03 00 12 00 25 01 00 09 48 69 6D 31 38 38 6D 6F 65 03 00 01 04 04 00 04 00 00 00 08 05 00 04 00 00 00 01 08 00 04 00 00 00 01

        /*
        val offset = unknownLength0 + fontLength//57
        event = MessageChain(PlainText(let {
            val length = input.readShortAt(101 + offset)//
            input.goto(103 + offset).readString(length.toInt())
        }))*/
    }
}