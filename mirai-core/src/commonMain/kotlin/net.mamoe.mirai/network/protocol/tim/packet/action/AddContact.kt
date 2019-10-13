@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.setId
import net.mamoe.mirai.utils.*

/**
 * 向服务器检查是否可添加某人为好友
 *
 * @author Him188moe
 */
@PacketId("00 A7")
class ClientCanAddFriendPacket(
        val bot: Long,
        val qq: Long,
        val sessionKey: ByteArray
) : ClientPacket() {
    override val idHex: String by lazy {
        super.idHex + " " + getRandomByteArray(2).toUHexString()
    }

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(bot)
        this.writeHex(TIMProtocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            writeQQ(qq)
        }
    }
}

@PacketId("00 A7")
class ServerCanAddFriendResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    lateinit var state: State

    enum class State {
        ALREADY_ADDED,
        REQUIRE_VERIFICATION,
        NOT_REQUIRE_VERIFICATION,
        FAILED,
    }


    override fun decode() {
        input.readBytes()
        val data = input.readRemainingBytes()
        if (data.size == 99) {
            state = State.ALREADY_ADDED
            return
        }
        state = when (data[data.size - 1].toUInt()) {
            0x00u -> State.NOT_REQUIRE_VERIFICATION
            0x01u -> State.REQUIRE_VERIFICATION
            0x99u -> State.ALREADY_ADDED
            0x03u,
            0x04u -> State.FAILED
            else -> throw IllegalArgumentException(data.contentToString())
        }
    }


    @PacketId("00 A7")
    class Encrypted(inputStream: ByteReadPacket) : ServerPacket(inputStream) {
        fun decrypt(sessionKey: ByteArray): ServerCanAddFriendResponsePacket {
            return ServerCanAddFriendResponsePacket(this.decryptBy(sessionKey)).setId(this.idHex)
        }
    }
}


/**
 * 请求添加好友
 */
@PacketId("00 AE")
class ClientAddFriendPacket(
        val bot: Long,
        val qq: Long,
        val sessionKey: ByteArray
) : ClientPacket() {
    override val idHex: String by lazy {
        super.idHex + " " + getRandomByteArray(2).toUHexString()
    }

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(bot)
        this.writeHex(TIMProtocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            writeHex("01 00 01")
            writeQQ(qq)
        }
    }

}


class ServerAddFriendResponsePacket(input: ByteReadPacket) : ServerAddContactResponsePacket(input)

class ServerAddGroupResponsePacket(input: ByteReadPacket) : ServerAddContactResponsePacket(input)

/**
 * 添加好友/群的回复
 */
abstract class ServerAddContactResponsePacket(input: ByteReadPacket) : ServerPacket(input) {


    class Raw(input: ByteReadPacket) : ServerPacket(input) {

        override fun decode() {

        }

        fun distribute(): ServerAddContactResponsePacket {

            TODO()
        }

        class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
            fun decrypt(sessionKey: ByteArray): Raw = Raw(this.decryptBy(sessionKey))
        }
    }


}