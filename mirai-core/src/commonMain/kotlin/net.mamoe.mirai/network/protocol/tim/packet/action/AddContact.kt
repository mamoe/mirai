@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUShort
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeQQ

/**
 * 向服务器检查是否可添加某人为好友
 *
 * @author Him188moe
 */
@Response(CanAddFriendPacket.Response::class)
@AnnotatedId(KnownPacketId.CAN_ADD_FRIEND)
object CanAddFriendPacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        qq: UInt,
        sessionKey: ByteArray
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeQQ(qq)
        }
    }

    @AnnotatedId(KnownPacketId.CAN_ADD_FRIEND)
    class Response(input: ByteReadPacket) : ResponsePacket(input) {
        lateinit var state: State

        enum class State {
            /**
             * 已经添加
             */
            ALREADY_ADDED,
            /**
             * 需要验证信息
             */
            REQUIRE_VERIFICATION,
            /**
             * 不需要验证信息
             */
            NOT_REQUIRE_VERIFICATION,

            /**
             * 对方拒绝添加
             */
            FAILED,
        }


        override fun decode() = with(input) {
            //需要验证信息 00 23 24 8B 00 01

            if (input.remaining > 20) {//todo check
                state = State.ALREADY_ADDED
                return
            }
            discardExact(4)//对方qq号
            state = when (val state = readUShort().toUInt()) {
                0x00u -> State.NOT_REQUIRE_VERIFICATION
                0x01u -> State.REQUIRE_VERIFICATION//需要验证信息
                0x99u -> State.ALREADY_ADDED

                0x03u,
                0x04u -> State.FAILED
                else -> throw IllegalStateException(state.toString())
            }
        }
    }
}

/**
 * 请求添加好友
 */
@AnnotatedId(KnownPacketId.CAN_ADD_FRIEND)
object AddFriendPacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        qq: UInt,
        sessionKey: ByteArray
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeHex("01 00 01")
            writeQQ(qq)
        }
    }
}

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
            fun decrypt(sessionKey: ByteArray): Raw = Raw(decryptBy(sessionKey))
        }
    }


}