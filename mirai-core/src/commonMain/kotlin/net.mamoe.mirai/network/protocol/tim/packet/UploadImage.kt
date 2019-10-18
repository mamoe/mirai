@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.account
import net.mamoe.mirai.network.protocol.tim.handler.ActionPacketHandler
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.*

@PacketId(0x03_88u)
expect class ClientTryGetImageIDPacket(
        botNumber: Long,
        sessionKey: ByteArray,
        groupNumberOrAccount: Long,
        image: PlatformImage
) : ClientPacket

@PacketId(0x03_88u)
sealed class ServerTryGetImageIDResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    @PacketId(0x03_88u)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerTryGetImageIDResponsePacket {
            val data = this.decryptAsByteArray(sessionKey)
            println("ServerTryGetImageIDResponsePacket.size=" + data.size)
            if (data.size == 209) {
                return ServerTryGetImageIDSuccessPacket(data.toReadPacket()).applySequence(sequenceId)
            }

            return ServerTryGetImageIDFailedPacket(data.toReadPacket()).applySequence(sequenceId)
        }
    }
}

/**
 * 服务器未存有图片, 返回一个 key 用于客户端上传
 */
@PacketId(0x03_88u)
class ServerTryGetImageIDSuccessPacket(input: ByteReadPacket) : ServerTryGetImageIDResponsePacket(input) {
    lateinit var uKey: ByteArray

    override fun decode() {
        this.input.gotoWhere(ubyteArrayOf(0x42u, 0x80u, 0x01u))//todo 优化
        uKey = this.input.readBytes(128)
        DebugLogger.logPurple("获得 uKey(128)=${uKey.toUHexString()}")
    }
}

/**
 * 服务器已经存有这个图片
 */
class ServerTryGetImageIDFailedPacket(input: ByteReadPacket) : ServerTryGetImageIDResponsePacket(input) {
    override fun decode(): Unit = with(input) {
        readRemainingBytes().debugPrint("ServerTryGetImageIDFailedPacket的body")
    }
}

suspend fun Group.uploadImage(imageId: String, image: PlatformImage) {
    this.bot.network[ActionPacketHandler].session.uploadGroupImage(number, imageId, image)
}

suspend fun QQ.uploadImage(imageId: String, image: PlatformImage) {
    TODO()
}

suspend fun BotSession.uploadGroupImage(groupNumberOrAccount: Long, imageId: String, image: PlatformImage) {
    ClientTryGetImageIDPacket(
            account,
            sessionKey,
            groupNumberOrAccount,
            image
    ).sendAndExpect<ServerTryGetImageIDResponsePacket> {
        when (it) {
            is ServerTryGetImageIDFailedPacket -> {
                //服务器已存有图片
            }
            is ServerTryGetImageIDSuccessPacket -> {
                val data = image.toByteArray()
                httpPostGroupImage(
                        uKeyHex = it.uKey.toUHexString(""),
                        botNumber = bot.qqAccount,
                        fileSize = data.size,
                        imageData = data,
                        groupCode = groupNumberOrAccount
                )
                //todo HTTP upload image.
            }
        }
    }.join()
}