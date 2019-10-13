package net.mamoe.mirai.message

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.protocol.tim.packet.ClientTryGetImageIDPacketJvm
import net.mamoe.mirai.network.protocol.tim.packet.ServerTryGetImageIDFailedPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerTryGetImageIDResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerTryGetImageIDSuccessPacket
import net.mamoe.mirai.qqNumber
import net.mamoe.mirai.utils.ImageNetworkUtils
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.toByteArray
import net.mamoe.mirai.utils.toUHexString
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 * 不确定是否存在于服务器的 [Image].
 * 必须在发送之前 [UnsolvedImage.upload] 或 [Contact.uploadImage], 否则会发送失败.
 *
 * @suppress todo 重新设计
 * @author Him188moe
 */
class UnsolvedImage(private val filename: String, val image: BufferedImage) {
    constructor(imageFile: File) : this(imageFile.name, ImageIO.read(imageFile))
    constructor(url: URL) : this(File(url.file))

    suspend fun upload(session: LoginSession, contact: Contact): CompletableDeferred<Unit> {
        return session.expectPacket<ServerTryGetImageIDResponsePacket> {
            toSend { ClientTryGetImageIDPacketJvm(session.bot.qqNumber, session.sessionKey, contact.number, image) }

            onExpect {
                when (it) {
                    is ServerTryGetImageIDFailedPacket -> {
                        //已经存在于服务器了
                    }

                    is ServerTryGetImageIDSuccessPacket -> {
                        val data = image.toByteArray()
                        withContext(Dispatchers.IO) {
                            if (!ImageNetworkUtils.postImage(it.uKey.toUHexString(), data.size, session.bot.qqNumber, contact.number, data)) {
                                throw RuntimeException("cannot upload image")
                            }
                        }
                    }
                }
            }
        }
    }

    fun toImage(): Image {
        return Image(getImageId(filename))
    }

    companion object {

        @JvmStatic
        fun getImageId(filename: String): String {
            val md5 = md5(filename)

            return "{" + md5.copyOfRange(0, 4).toUHexString("") + "-"
                    .plus(md5.copyOfRange(4, 6).toUHexString("")) + "-"
                    .plus(md5.copyOfRange(6, 8).toUHexString("")) + "-"
                    .plus(md5.copyOfRange(8, 10).toUHexString("")) + "-"
                    .plus(md5.copyOfRange(10, 16).toUHexString("")) + "}." + if (filename.endsWith(".jpeg")) "jpg" else filename.substringAfter(".", "jpg")
        }
    }
}