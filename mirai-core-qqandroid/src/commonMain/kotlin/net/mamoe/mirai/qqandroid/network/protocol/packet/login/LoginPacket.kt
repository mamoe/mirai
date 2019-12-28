package net.mamoe.mirai.qqandroid.network.protocol.packet.login


import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.network.QQAndroidDevice
import net.mamoe.mirai.qqandroid.network.protocol.packet.EncryptMethod
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.tlv.writeTLVList
import net.mamoe.mirai.utils.cryptor.DecrypterByteArray
import net.mamoe.mirai.utils.cryptor.DecrypterType

class LoginPacketDecrypter(override val value: ByteArray) : DecrypterByteArray {
    companion object : DecrypterType<LoginPacketDecrypter> {

    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
object LoginPacket : PacketFactory<LoginPacket.LoginPacketResponse, LoginPacketDecrypter>(LoginPacketDecrypter) {

    fun invoke(
        device: QQAndroidDevice
    ): OutgoingPacket = buildOutgoingPacket(device, EncryptMethod.ByECDH135) {
        writeTLVList {

        }
    }


    class LoginPacketResponse : Packet

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): LoginPacketResponse {

        TODO()
    }
}

interface PacketId {
    val commandId: Int // ushort actually
    val subCommandId: Int // ushort actually
}

object NullPacketId : PacketId {
    override val commandId: Int
        get() = error("uninitialized")
    override val subCommandId: Int
        get() = error("uninitialized")
}
