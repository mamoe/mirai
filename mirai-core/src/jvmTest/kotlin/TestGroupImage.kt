@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

import net.mamoe.mirai.network.protocol.tim.packet.GroupImageIdRequestPacket
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.io.readRemainingBytes
import net.mamoe.mirai.utils.io.toUHexString
import net.mamoe.mirai.utils.toMiraiImage
import java.io.File
import javax.imageio.ImageIO

val sessionKey: ByteArray = "F1 ED F2 BC 55 17 7B FE CC CC F3 08 D1 8D A7 0E".hexToBytes()

fun main() = println({
    val image = ImageIO.read(File("C:\\Users\\Him18\\Desktop\\test2.gif").readBytes().inputStream()).toMiraiImage("png")

    // File("C:\\Users\\Him18\\Desktop\\test2.jpg").writeBytes(image.fileData.readBytes())
    GroupImageIdRequestPacket(
            1994701021u,
            580266363u,
            image,
            sessionKey
    ).packet.readRemainingBytes().toUHexString()
}())
