package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.packet.image.ClientTryGetImageIDPacket
import net.mamoe.mirai.network.packet.image.ServerTryGetImageIDFailedPacket
import net.mamoe.mirai.network.packet.image.ServerTryGetImageIDResponsePacket
import net.mamoe.mirai.network.packet.image.ServerTryGetImageIDSuccessPacket
import net.mamoe.mirai.network.packet.md5
import net.mamoe.mirai.utils.ImageNetworkUtils
import net.mamoe.mirai.utils.toByteArray
import net.mamoe.mirai.utils.toUHexString
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

/**
 * 不确定是否存在于服务器的 [Image].
 * 必须在发送之前 [UnsolvedImage.upload] 或 [Contact.uploadImage], 否则会发送失败.
x *
 * @author Him188moe
 */
class UnsolvedImage(filename: String, val image: BufferedImage) : Image(getImageId(filename)) {
    constructor(imageFile: File) : this(imageFile.name, ImageIO.read(imageFile))
    constructor(url: URL) : this(File(url.file))

    suspend fun upload(session: LoginSession, contact: Contact): CompletableFuture<Unit> {//todo be suspend
        return session.expectPacket<ServerTryGetImageIDResponsePacket> {
            toSend { ClientTryGetImageIDPacket(session.bot.account.qqNumber, session.sessionKey, contact.number, image) }

            expect {
                when (it) {
                    is ServerTryGetImageIDFailedPacket -> {
                        //已经存在于服务器了
                    }

                    is ServerTryGetImageIDSuccessPacket -> {
                        val data = image.toByteArray()
                        if (!ImageNetworkUtils.postImage(it.uKey.toUHexString(), data.size, session.bot.account.qqNumber, contact.number, data)) {
                            throw RuntimeException("cannot upload image")
                        }
                    }
                }
            }
        }
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