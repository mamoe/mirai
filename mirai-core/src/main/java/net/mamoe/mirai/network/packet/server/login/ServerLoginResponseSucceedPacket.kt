package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.hexToShort
import java.io.DataInputStream

/**
 * @author Him188moe @ Mirai Project
 * @author NaturalHG @ Mirai Project
 */
class ServerLoginResponseSucceedPacket(input: DataInputStream, val tgtgtKey: ByteArray) : ServerPacket(input) {
    lateinit var _0828_rec_decr_key: ByteArray
    var age: Int = 0
    var gender: Boolean = false//from 1byte
    lateinit var nick: String
    lateinit var clientKey: String


    @ExperimentalUnsignedTypes
    override fun decode() {
        input.skip(21)
        val data = input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }//Drop tail

        val decryptedData = TEACryptor.decrypt(TEACryptor.decrypt(data, Protocol.shareKey.hexToBytes()), tgtgtKey);


        DataInputStream(decryptedData.inputStream()).let {
            //换成 readShort

            it.skip(212)
            val msgLength = when (it.readShort()) {
                "01 07".hexToShort() -> 0
                "00 33".hexToShort() -> 28 * 3
                "01 10".hexToShort() -> 64 * 3
                else -> throw IllegalStateException()
            }

            age = it.readShort(取文本中间(it, 取文本长度(it) - 82, 5)).toBoolean()
            gender = it.readByte(取文本中间(it, 取文本长度(it) - 94, 2))

            var position = ((514 + msgLength) / 2 - 212 - 2).toLong();
            it.skip(position)
            _0828_rec_decr_key = it.readNBytes(13)
            it.skip((1873 + msgLength) / 2 - position)
            position += (1873 + msgLength) / 2

            nick = it.readNBytes(it.readByte().toInt()).toString()


            clientKey = it.readBytes(取文本中间(it, 484 * 3 + msgLength + 1, 112 * 3 - 1))
        }


        //SendUdp (Construct_0828 (“04 34”, 取文本中间 (data, 76, 167), 取文本中间 (data, 568 + msg_length, 407), 取文本中间 (data, 22, 47)))
    }
}