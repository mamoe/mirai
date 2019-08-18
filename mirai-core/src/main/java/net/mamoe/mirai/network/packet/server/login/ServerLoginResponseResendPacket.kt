package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.network.packet.server.goto
import net.mamoe.mirai.util.TEACryptor
import java.io.DataInputStream

/**
 * @author Him188moe
 */
@PacketId("08 36 31 03")
class ServerLoginResponseResendPacket(input: DataInputStream, val flag: Flag) : ServerPacket(input) {
    enum class Flag {
        `08 36 31 03`,
        OTHER,
    }

    lateinit var _0836_tlv0006_encr: ByteArray;
    lateinit var token: ByteArray
    lateinit var tgtgtKey: ByteArray

    override fun decode() {//todo 检查
        this.input.skip(5)
        tgtgtKey = this.input.readNBytes(16)//22
        this.input.skip(3)//25
        _0836_tlv0006_encr = this.input.readNBytes(120)

        when (flag) {
            Flag.`08 36 31 03` -> {
                token = this.input.goto(153).readNBytes(56)
            }

            Flag.OTHER -> {
                //do nothing in this packet.
                //[this.token] will be set in [Robot]
                //token
            }
        }
    }
}

class ServerLoginResponseResendPacketEncrypted(input: DataInputStream, private val flag: ServerLoginResponseResendPacket.Flag) : ServerPacket(input) {
    override fun decode() {

    }

    fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseResendPacket {
        this.input goto 14
        var data: ByteArray = this.input.readAllBytes()
        data = TEACryptor.CRYPTOR_SHARE_KEY.decrypt(data.let { it.copyOfRange(0, it.size - 1) });
        data = TEACryptor.decrypt(data, tgtgtKey);
        return ServerLoginResponseResendPacket(data.dataInputStream(), flag)
    }
}