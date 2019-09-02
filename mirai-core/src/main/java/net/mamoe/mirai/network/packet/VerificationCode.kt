package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.*
import java.io.DataInputStream


@ExperimentalUnsignedTypes
@PacketId("00 BA 31")
class ClientVerificationCodeTransmissionRequestPacket(//todo 这个包可能有问题.
        private val count: Int,
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSequence: Int,
        private val token00BA: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeByte(count)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol._fixVer)
        this.writeHex(Protocol._00BaKey)
        this.encryptAndWrite(Protocol._00BaKey) {
            it.writeHex("00 02 00 00 08 04 01 E0")
            it.writeHex(Protocol._0825data2)
            it.writeHex("00 00 38")
            it.write(token0825)
            it.writeHex("01 03 00 19")
            it.writeHex(Protocol.publicKey)
            it.writeHex("13 00 05 00 00 00 00")
            it.writeByte(verificationSequence)
            it.writeHex("00 28")
            it.write(token00BA)
            it.writeHex("00 10")
            it.writeHex(Protocol._00BaFixKey)
        }
    }
}

/**
 * @author Him188moe
 */
class ServerVerificationCodeTransmissionPacket(input: DataInputStream, val dataSize: Int, val packetId: ByteArray) : ServerVerificationCodePacket(input) {

    lateinit var verificationCodePart2: ByteArray
    lateinit var verificationToken: ByteArray//56bytes
    var transmissionCompleted: Boolean = false//验证码是否已经传输完成
    lateinit var token00BA: ByteArray//40 bytes
    var count: Int = 0

    @ExperimentalUnsignedTypes
    override fun decode() {
        this.verificationToken = this.input.readNBytesAt(10, 56)

        val length = this.input.readShortAt(66)
        this.verificationCodePart2 = this.input.readNBytes(length)

        this.input.skip(2)
        this.transmissionCompleted = this.input.readBoolean()

        this.token00BA = this.input.readNBytesAt(dataSize - 57, 40)
        this.count = byteArrayOf(0, 0, packetId[2], packetId[3]).toUHexString().hexToInt()
    }
}

/**
 * @author Him188moe
 */
class ServerVerificationCodeRepeatPacket(input: DataInputStream) : ServerVerificationCodePacket(input) {

    lateinit var token00BA: ByteArray//56 bytes
    lateinit var tgtgtKeyUpdate: ByteArray

    @ExperimentalUnsignedTypes
    override fun decode() {
        token00BA = this.input.readNBytesAt(10, 56)
        tgtgtKeyUpdate = getRandomKey(16)
    }
}

abstract class ServerVerificationCodePacket(input: DataInputStream) : ServerPacket(input)

class ServerVerificationCodePacketEncrypted(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }

    @ExperimentalUnsignedTypes
    fun decrypt(): ServerVerificationCodePacket {
        this.input goto 14
        val data = TEACryptor.decrypt(Protocol._00BaKey.hexToBytes(), this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) })
        return if (data.size == 95) {
            ServerVerificationCodeRepeatPacket(data.dataInputStream())
        } else {
            ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, this.input.readNBytesAt(3, 4))
        }
    }
}