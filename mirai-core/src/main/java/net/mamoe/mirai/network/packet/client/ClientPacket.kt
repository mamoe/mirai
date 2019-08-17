package net.mamoe.mirai.network.packet.client

import lombok.Getter
import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.util.ByteArrayDataOutputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * @author Him188moe @ Mirai Project
 */
abstract class ClientPacket : ByteArrayDataOutputStream(), Packet {
    @Getter
    val packageId: Int

    init {
        val annotation = this.javaClass.getAnnotation(PacketId::class.java)
        packageId = annotation.value

        try {
            this.writeHex(Protocol.head)
            this.writeHex(Protocol.ver)
            this.writePacketId()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @Throws(IOException::class)
    fun writePacketId() {
        this.writeInt(this@ClientPacket.packageId)
    }

    /**
     * Encode this packet.
     *
     *
     * Before sending the packet, an [tail][Protocol.tail] will be added.
     */
    @Throws(IOException::class)
    abstract fun encode()

    @Throws(IOException::class)
    fun encodeToByteArray(): ByteArray {
        encode()
        return toByteArray()
    }

}


@Throws(IOException::class)
fun DataOutputStream.writeIp(ip: String) {
    for (s in ip.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        this.writeByte(s.toInt())
    }
}


@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeHex(hex: String) {
    for (s in hex.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        if (s.isEmpty()) {
            continue
        }
        this.writeByte(s.toUByte(16).toByte().toInt())
    }
}

@Throws(IOException::class)
fun DataOutputStream.writeRandom(length: Int) {
    for (i in 0 until length) {
        this.writeByte((Math.random() * 255).toInt().toByte().toInt())
    }
}

@Throws(IOException::class)
fun DataOutputStream.writeQQ(qq: Int) {
    this.writeInt(qq)
}