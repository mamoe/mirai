package net.mamoe.mirai.network.packet.server.security

import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.util.TEACryptor
import java.io.DataInputStream

/**
 * Dispose_0828
 *
 * @author Him188moe @ Mirai Project
 */
class ServerSessionKeyResponsePacket(inputStream: DataInputStream) : ServerPacket(inputStream) {
    override fun decode() {
        var data = this.input.readAllBytes();
        when (data.size) {
            407 -> {
            }

            439 -> {

            }

            527 -> {

            }
            else -> {
            }
        }

                .判断开始(length ＝ 407)
        g_sessionKey ＝ 取文本中间 (data, 76, 47)
        .判断(length ＝ 439)
        g_sessionKey ＝ 取文本中间 (data, 190, 47)
        .判断(length ＝ 527)
        g_sessionKey ＝ 取文本中间 (data, 190, 47)
        g_tlv0105 ＝ “01 05 00 88 00 01 01 02 ” ＋ “00 40 02 01 03 3C 01 03 00 00 ” ＋ 取文本中间 (data, 取文本长度 (data) － 367, 167) ＋ “ 00 40 02 02 03 3C 01 03 00 00 ” ＋ 取文本中间 (data, 取文本长度 (data) － 166, 167)

    }
}

/**
 * Encrypted using []0828_rec_decr_key], decrypting in Robot
 *
 * @author Him188moe @ Mirai Project
 */
class ServerSessionKeyResponsePacketEncrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
    override fun decode() {

    }

    fun decrypt(_0828_rec_decr_key: ByteArray): ServerSessionKeyResponsePacket {//todo test
        this.input.skip(14)
        return ServerSessionKeyResponsePacket(TEACryptor.decrypt(this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }, _0828_rec_decr_key).dataInputStream());
        //TeaDecrypt(取文本中间(data, 43, 取文本长度(data) － 45), m_0828_rec_decr_key)
    }
}