@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import net.mamoe.mirai.utils.OnlineStatus
import kotlin.properties.Delegates

/**
 * 好友在线状态改变
 *
 * TODO 真的是在线状态改变么
 */
@AnnotatedId(KnownPacketId.SEND_FRIEND_MESSAGE)
class FriendOnlineStatusChangedPacket(input: ByteReadPacket) : ServerPacket(input) {
    var qq: UInt by Delegates.notNull()
    lateinit var status: OnlineStatus

    override fun decode() = with(input) {
        qq = readUInt()
        discardExact(8)
        val id = readUByte()
        status = OnlineStatus.ofId(id) ?: error("Unknown online status id $id")
    }

    //在线     XX XX XX XX 01 00 00 00 00 00 00 00 0A 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
    //忙碌     XX XX XX XX 01 00 00 00 00 00 00 00 32 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00

    @AnnotatedId(KnownPacketId.SEND_FRIEND_MESSAGE)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): FriendOnlineStatusChangedPacket =
            FriendOnlineStatusChangedPacket(this.decryptBy(sessionKey)).applySequence(sequenceId)
    }
}