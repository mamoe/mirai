/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.writeFully
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.utils.cryptor.ECDH
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeShortLVByteArray

/**
 * Encryption method to be used for packet body.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
internal interface EncryptMethod {
    val id: Int

    fun makeBody(client: QQAndroidClient, body: BytePacketBuilder.() -> Unit): ByteReadPacket
}

internal interface EncryptMethodSessionKey : EncryptMethod {
    override val id: Int get() = 69
    val currentLoginState: Int
    val sessionKey: ByteArray

    /**
     * buildPacket{
     *     byte 1
     *     byte if (currentLoginState == 2) 3 else 2
     *     fully key
     *     short 258
     *     short 0
     *     fully encrypted
     * }
     */
    override fun makeBody(client: QQAndroidClient, body: BytePacketBuilder.() -> Unit): ByteReadPacket =
        buildPacket {
            require(currentLoginState == 2 || currentLoginState == 3) { "currentLoginState must be either 2 or 3" }
            writeByte(1) // const
            writeByte(if (currentLoginState == 2) 3 else 2)
            writeFully(sessionKey)
            writeShort(258) // const
            writeShort(0) // const, length of publicKey
            encryptAndWrite(sessionKey, body)
        }
}

inline class EncryptMethodSessionKeyLoginState2(override val sessionKey: ByteArray) :
    EncryptMethodSessionKey {
    override val currentLoginState: Int get() = 2
}

inline class EncryptMethodSessionKeyLoginState3(override val sessionKey: ByteArray) :
    EncryptMethodSessionKey {
    override val currentLoginState: Int get() = 3
}

inline class EncryptMethodECDH135(override val ecdh: ECDH) :
    EncryptMethodECDH {
    override val id: Int get() = 135
}

inline class EncryptMethodECDH7(override val ecdh: ECDH) :
    EncryptMethodECDH {
    override val id: Int get() = 7
}

internal interface EncryptMethodECDH : EncryptMethod {
    val ecdh: ECDH

    /**
     * **Packet Structure**
     * byte     1
     * byte     1
     * byte[]   [ECDH.privateKey]
     * short    258
     * short    [ECDH.publicKey].size
     * byte[]   [ECDH.publicKey]
     * byte[]   encrypted `body()` by [ECDH.shareKey]
     */
    override fun makeBody(client: QQAndroidClient, body: BytePacketBuilder.() -> Unit): ByteReadPacket =
        buildPacket {
            writeByte(1) // const
            writeByte(1) // const
            writeFully(client.randomKey)
            writeShort(258) // const

            // writeShortLVByteArray("04 CB 36 66 98 56 1E 93 6E 80 C1 57 E0 74 CA B1 3B 0B B6 8D DE B2 82 45 48 A1 B1 8D D4 FB 61 22 AF E1 2F E4 8C 52 66 D8 D7 26 9D 76 51 A8 EB 6F E7".hexToBytes())

            writeShortLVByteArray(ecdh.keyPair.publicKey.getEncoded().drop(23).take(49).toByteArray().also {
                // it.toUHexString().debugPrint("PUBLIC KEY")
                check(it[0].toInt() == 0x04) { "Bad publicKey generated. Expected first element=0x04, got${it[0]}" }
                //check(ecdh.calculateShareKeyByPeerPublicKey(it.adjustToPublicKey()).contentEquals(ecdh.keyPair.shareKey)) { "PublicKey Validation failed" }
            })

            // encryptAndWrite("26 33 BA EC 86 EB 79 E6 BC E0 20 06 5E A9 56 6C".hexToBytes(), body)
            encryptAndWrite(ecdh.keyPair.initialShareKey, body)
        }
}