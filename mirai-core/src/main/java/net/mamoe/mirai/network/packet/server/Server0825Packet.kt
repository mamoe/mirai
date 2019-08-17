package net.mamoe.mirai.network.packet.server

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.getRandomKey
import java.io.DataInputStream

/**
 * A packet received when logging in, used to redirect server address
 *
 * @author Him188moe @ Mirai Project
 */
class Server0825Packet(private val type: Type, inputStream: DataInputStream) : ServerPacket(inputStream) {
    lateinit var serverIP: String;

    var loginTime: Long = 0;
    lateinit var loginIP: String;
    lateinit var token: ByteArray;
    lateinit var tgtgtKey: ByteArray

    enum class Type {
        TYPE_08_25_31_01,
        TYPE_08_25_31_02,
    }

    @ExperimentalUnsignedTypes
    override fun decode() {
        input.skip(43 - 11)//todo: check
        val data = DataInputStream(TEACryptor.decrypt(input.readAllBytes().let { it.copyOfRange(0, it.size - 2) }, when (type) {//todo: check array range
            Type.TYPE_08_25_31_01 -> Protocol.redirectionKey.toByteArray()
            Type.TYPE_08_25_31_02 -> Protocol._0825key.toByteArray()
        }).inputStream());

        when (data.readByte().toInt()) {
            0xFE -> {
                serverIP = data.readIP()
            }
            0X00 -> {
                data.skip(16 - 2)
                token = data.readNBytes(167 - (16 - 2))
                loginTime = data.readLong()//todo check
                loginIP = data.readIP()
                tgtgtKey = getRandomKey(16);
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }
}