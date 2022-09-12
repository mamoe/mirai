/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.utils.crypto.QQEcdh
import net.mamoe.mirai.internal.utils.io.encryptAndWrite
import net.mamoe.mirai.internal.utils.io.writeShortLVByteArray

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

internal class EncryptMethodSessionKeyNew(
    val wtSessionTicket: ByteArray, // t133
    val wtSessionTicketKey: ByteArray, // t134
) : EncryptMethod {
    override val id: Int get() = 69

    override fun makeBody(client: QQAndroidClient, body: BytePacketBuilder.() -> Unit): ByteReadPacket =
        buildPacket {
            writeShortLVByteArray(wtSessionTicket)
            encryptAndWrite(wtSessionTicketKey, body)
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

internal class EncryptMethodEcdh135(override val ecdh: QQEcdh) :
    EncryptMethodEcdh {
    override val id: Int get() = 135
}

internal class EncryptMethodEcdh7(override val ecdh: QQEcdh) :
    EncryptMethodEcdh {
    override val id: Int get() = 7 // 135
}

internal interface EncryptMethodEcdh : EncryptMethod {
    companion object {
        operator fun invoke(ecdh: QQEcdh): EncryptMethodEcdh {
            return if (ecdh.fallbackMode) {
                EncryptMethodEcdh135(ecdh)
            } else EncryptMethodEcdh7(ecdh)
        }
    }

    val ecdh: QQEcdh

    override fun makeBody(client: QQAndroidClient, body: BytePacketBuilder.() -> Unit): ByteReadPacket = buildPacket {
        /* //new curve p-256
        writeByte(2) // const
        writeByte(1) // const
        writeFully(client.randomKey)
        writeShort(0x0131) // const
        writeShort(0x0001)
         */

        writeByte(2) // version
        writeByte(1) // const
        writeFully(client.randomKey)
        writeShort(0x0131)
        writeShort(ecdh.version.toShort())// public key version
        // for p-256, drop(26). // but not really sure.
        writeShortLVByteArray(ecdh.publicKey)
        encryptAndWrite(ecdh.initialQQShareKey, body)
    }
}