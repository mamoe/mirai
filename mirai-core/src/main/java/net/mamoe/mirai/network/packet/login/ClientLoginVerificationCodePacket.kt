package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.util.ByteArrayDataOutputStream
import net.mamoe.mirai.util.TEACryptor

/**
 * @author Him188moe
 */
@PacketId("00 BA 31 01")
@ExperimentalUnsignedTypes
class ClientLoginVerificationCodePacket(
        private val qq: Int,
        private val token0825: ByteArray,
        private val sequence: Int,
        private val token00BA: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.writeHex(Protocol._00BaKey)
        this.write(TEACryptor.CRYPTOR_00BAKEY.encrypt(object : ByteArrayDataOutputStream() {
            override fun toByteArray(): ByteArray {
                this.writeHex("00 02 00 00 08 04 01 E0")
                this.writeHex(Protocol._0825data2)
                this.writeHex("00 00 38")
                this.write(token0825)
                this.writeHex("01 03 00 19")
                this.writeHex(Protocol.publicKey)
                this.writeHex("13 00 05 00 00 00 00")
                this.writeVarInt(sequence.toUInt())
                this.writeHex("00 28")
                this.write(token00BA)
                this.writeHex("00 10")
                this.writeHex(Protocol._00BaFixKey)
                return super.toByteArray()
            }
        }.toByteArray()))
    }
}