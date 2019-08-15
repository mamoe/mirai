package net.mamoe.mirai.network.packet.server

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.util.TEAEncryption
import java.io.DataInputStream

/**
 * A packet received when logging in, used to redirect server address
 *
 * @author Him188moe @ Mirai Project
 */
class Server0825Packet(private val type: Type, inputStream: DataInputStream) : ServerPacket(inputStream) {
    lateinit var serverIP: String;

    enum class Type {
        TYPE_08_25_31_01,
        TYPE_08_25_31_02,
    }

    override fun decode() {
        input.skip(43 - 11)//todo: check
        val data = DataInputStream(TEAEncryption.decrypt(input.readAllBytes().let { it.copyOfRange(0, it.size - 45) }, when (type) {
            Type.TYPE_08_25_31_01 -> Protocol.redirectionKey.toByteArray()
            Type.TYPE_08_25_31_02 -> Protocol._0825key.toByteArray()
        }).inputStream());

        when (data.readByte().toInt()) {
            0xFE -> {
                serverIP = data.readIP()
            }
            0X00 -> {

            }
            else -> {
            }
        }
    }
}