@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.utils.gotoWhere
import net.mamoe.mirai.utils.toReadPacket

expect class PlatformImage

expect class ClientTryGetImageIDPacket(
        botNumber: Long,
        sessionKey: ByteArray,
        groupNumberOrQQNumber: Long,
        image: PlatformImage
) : ClientPacket


abstract class ServerTryGetImageIDResponsePacket(input: ByteReadPacket) : ServerPacket(input) {

    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerTryGetImageIDResponsePacket {
            val data = this.decryptAsByteArray(sessionKey)
            println(data.size)
            println(data.size)
            if (data.size == 209) {
                return ServerTryGetImageIDSuccessPacket(data.toReadPacket()).applySequence(sequenceId)
            }

            return ServerTryGetImageIDFailedPacket(data.toReadPacket())
        }
    }
}

/**
 * 服务器未存有图片, 返回一个 key 用于客户端上传
 */
class ServerTryGetImageIDSuccessPacket(input: ByteReadPacket) : ServerTryGetImageIDResponsePacket(input) {
    lateinit var uKey: ByteArray

    override fun decode() {
        this.input.gotoWhere(ubyteArrayOf(0x42u, 0x80u, 0x01u))
        uKey = this.input.readBytes(128)
    }
}

/**
 * 服务器已经存有这个图片
 */
class ServerTryGetImageIDFailedPacket(input: ByteReadPacket) : ServerTryGetImageIDResponsePacket(input) {
    override fun decode() {

    }
}