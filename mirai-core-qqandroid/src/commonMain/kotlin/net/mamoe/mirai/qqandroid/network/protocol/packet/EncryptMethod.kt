/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.writeFully
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.utils.cryptor.ECDH
import net.mamoe.mirai.qqandroid.utils.cryptor.ECDHKeyPair
import net.mamoe.mirai.qqandroid.utils.io.encryptAndWrite
import net.mamoe.mirai.qqandroid.utils.io.writeShortLVByteArray

internal interface EncryptMethod {
    val id: Int

    fun makeBody(client: QQAndroidClient, body: BytePacketBuilder.() -> Unit): ByteReadPacket
}

internal interface EncryptMethodSessionKey : EncryptMethod {
    override val id: Int get() = 69
    val currentLoginState: Int
    val sessionKey: ByteArray

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

internal class EncryptMethodSessionKeyLoginState2(override val sessionKey: ByteArray) :
    EncryptMethodSessionKey {
    override val currentLoginState: Int get() = 2
}

internal class EncryptMethodSessionKeyLoginState3(override val sessionKey: ByteArray) :
    EncryptMethodSessionKey {
    override val currentLoginState: Int get() = 3
}

internal class EncryptMethodECDH135(override val ecdh: ECDH) :
    EncryptMethodECDH {
    override val id: Int get() = 135
}

internal class EncryptMethodECDH7(override val ecdh: ECDH) :
    EncryptMethodECDH {
    override val id: Int get() = 7
}

internal interface EncryptMethodECDH : EncryptMethod {
    companion object {
        operator fun invoke(ecdh: ECDH): EncryptMethodECDH {
            return if (ecdh.keyPair === ECDHKeyPair.DefaultStub) {
                EncryptMethodECDH135(ecdh)
            } else EncryptMethodECDH7(ecdh)
        }
    }

    val ecdh: ECDH

    override fun makeBody(client: QQAndroidClient, body: BytePacketBuilder.() -> Unit): ByteReadPacket =
        buildPacket {
            writeByte(1) // const
            writeByte(1) // const
            writeFully(client.randomKey)
            writeShort(258) // const

            if (ecdh.keyPair === ECDHKeyPair.DefaultStub) {
                writeShortLVByteArray(ECDHKeyPair.DefaultStub.defaultPublicKey)
                encryptAndWrite(ECDHKeyPair.DefaultStub.defaultShareKey, body)
            } else {
                writeShortLVByteArray(ecdh.keyPair.publicKey.getEncoded().drop(23).take(49).toByteArray().also {
                    check(it[0].toInt() == 0x04) { "Bad publicKey generated. Expected first element=0x04, got${it[0]}" }
                })

                encryptAndWrite(ecdh.keyPair.initialShareKey, body)
            }
        }
}