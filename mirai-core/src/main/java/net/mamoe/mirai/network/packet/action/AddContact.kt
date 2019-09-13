@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.packet.action

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.getRandomByteArray
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream
import java.util.*

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
    val packetIdLast = getRandomByteArray(2)

    override fun getFixedId(): String {
        return idHex + " " + packetIdLast.toUHexString()
    }

    override fun encode() {
        this.write(packetIdLast)//id, 2bytes

        this.writeQQ(bot)
        this.writeHex(Protocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            it.writeQQ(qq)
        }
    }
}

@PacketId("00 A7")
class ServerCanAddFriendResponsePacket(input: DataInputStream) : ServerPacket(input) {
    lateinit var state: State

    enum class State {
        ALREADY_ADDED,
        REQUIRE_VERIFICATION,
        NOT_REQUIRE_VERIFICATION,
        FAILED,
    }


    override fun decode() {
        val data = input.goto(0).readAllBytes()
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
            else -> throw IllegalArgumentException(Arrays.toString(data))
        }
    }


    @PacketId("00 A7")
    class Encrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
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
    val packetIdLast = getRandomByteArray(2)

    override fun getFixedId(): String {
        return idHex + " " + packetIdLast.toUHexString()
    }

    override fun encode() {
        this.write(packetIdLast)//id, 2bytes

        this.writeQQ(bot)
        this.writeHex(Protocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("01 00 01")
            it.writeQQ(qq)
        }
    }

}


class ServerAddFriendResponsePacket(input: DataInputStream) : ServerAddContactResponsePacket(input)

class ServerAddGroupResponsePacket(input: DataInputStream) : ServerAddContactResponsePacket(input)

/**
 * 添加好友/群的回复
 */
abstract class ServerAddContactResponsePacket(input: DataInputStream) : ServerPacket(input) {


    class Raw(input: DataInputStream) : ServerPacket(input) {

        override fun decode() {

        }

        fun distribute(): ServerAddContactResponsePacket {

            TODO()
        }

        class Encrypted(input: DataInputStream) : ServerPacket(input) {
            fun decrypt(sessionKey: ByteArray): Raw = Raw(this.decryptBy(sessionKey))
        }
    }


}