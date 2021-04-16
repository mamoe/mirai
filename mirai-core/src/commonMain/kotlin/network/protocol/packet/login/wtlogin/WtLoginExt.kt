/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin

import kotlinx.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.LoginExtraData
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.protocol.packet.Tlv
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.network.protocol.packet.t145
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.writeShortLVByteArray
import net.mamoe.mirai.utils.*


internal inline fun WtLoginExt.analysisTlv0x531(
    t531: ByteArray,
    handler: (a1: ByteArray, noPicSig: ByteArray) -> Unit
) {
    val map = t531.toReadPacket().withUse { _readTLVMap() }

    val t106 = map[0x106]
    val t16a = map[0x16a]
    val t113 = map[0x113]
    val t10c = map[0x10c]

    if (t106 != null && t16a != null && t113 != null && t10c != null) {
        handler(t106 + t10c, t16a)
    }
}

internal interface WtLoginExt { // so as not to register to global extension

    fun onErrorMessage(type: Int, tlvMap: TlvMap, bot: QQAndroidBot): WtLogin.Login.LoginPacketResponse.Error? {
        return tlvMap[0x149]?.read {
            discardExact(2) //type
            val title: String = readUShortLVString()
            val content: String = readUShortLVString()
            val otherInfo: String = readUShortLVString()

            WtLogin.Login.LoginPacketResponse.Error(bot, type, title, content, otherInfo)
        } ?: tlvMap[0x146]?.read {
            discardExact(2) // ver
            discardExact(2)  // code

            val title = readUShortLVString()
            val message = readUShortLVString()
            val errorInfo = readUShortLVString()

            WtLogin.Login.LoginPacketResponse.Error(bot, type, title, message, errorInfo)
        }
    }

    fun TlvMap.getOrEmpty(key: Int): ByteArray {
        return this[key] ?: byteArrayOf()
    }

    /**
     * @throws error
     */
    fun QQAndroidClient.parseWFastLoginInfoDataOutA1(t169: ByteArray): ByteReadPacket {
        val map = t169.toReadPacket().withUse { _readTLVMap() }

        val t106 = map[0x106]
        val t10c = map[0x10c]
        val t16a = map[0x16a]

        check(t106 != null) { "getWFastLoginInfoDataOutA1: Cannot find tlv 0x106!!" }
        check(t10c != null) { "getWFastLoginInfoDataOutA1: Cannot find tlv 0x10c!!" }
        check(t16a != null) { "getWFastLoginInfoDataOutA1: Cannot find tlv 0x16a!!" }

        return buildPacket {
            writeByte(64)
            writeShort(4)

            // TLV
            writeShort(0x106)
            writeShortLVByteArray(t106)

            writeShort(0x10c)
            writeShortLVByteArray(t10c)

            writeShort(0x16a)
            writeShortLVByteArray(t16a)

            t145(device.guid)
        }
    }

    /**
     * login extra data
     *
     * oicq/wlogin_sdk/request/oicq_request.java:1445
     */
    fun QQAndroidClient.analysisTlv537(t537: ByteArray) = t537.read {
        //discardExact(2)
        discardExact(1)
        repeat(readByte().toInt()) {
            loginExtraData.add(
                LoginExtraData( // args are to correct order
                    uin = readLong(),
                    ip = readBytes(readByte().toInt() and 0xff),
                    time = readInt(), // correct
                    version = readInt()
                )
            )
        }
    }

    /**
     * Encrypt sig and key for pic downloading
     */
    fun QQAndroidClient.analysisTlv11d(t11d: ByteArray): WLoginSigInfo.EncryptedDownloadSession = t11d.read {
        val appid = readInt().toLong().and(4294967295L)
        val stKey = ByteArray(16)
        readAvailable(stKey)
        val stSigLength = readUShort().toInt()
        val stSig = ByteArray(stSigLength)
        readAvailable(stSig)
        WLoginSigInfo.EncryptedDownloadSession(
            appid,
            stKey,
            stSig
        )
    }

    /**
     * pwd flag
     */
    fun QQAndroidClient.analysisTlv186(t186: ByteArray) = t186.read {
        discardExact(1)
        pwdFlag = readByte().toInt() == 1
    }

    /**
     * 设置 [QQAndroidClient.uin]
     */
    fun QQAndroidClient.analysisTlv113(t113: ByteArray) = t113.read {
        _uin = readUInt().toLong()

        /*
        // nothing to do

          if (!asyncContext.ifQQLoginInQim(class_1048.productType)) {
              this.field_61436.method_62330(this.field_61436.field_63973, this.field_61436.uin);
          }
         */
    }

    /**
     * 设置 [QQAndroidClient.timeDifference] 和 [QQAndroidClient.ipFromT149]
     */
    fun QQAndroidClient.analysisTlv130(t130: ByteArray) = t130.read {
        discardExact(2)
        timeDifference = readUInt().toLong() - currentTimeSeconds()
        ipFromT149 = readBytes(4)
    }

    fun QQAndroidClient.analysisTlv150(t150: ByteArray) {
        this.t150 = Tlv(t150)
    }

    fun QQAndroidClient.analysisTlv161(t161: ByteArray) {
        val tlv = t161.toReadPacket().apply { discardExact(2) }.withUse { _readTLVMap() }

        tlv[0x173]?.let { analysisTlv173(it) }
        tlv[0x17f]?.let { analysisTlv17f(it) }
        tlv[0x172]?.let { rollbackSig = it }
    }

    /**
     * server host
     */
    fun QQAndroidClient.analysisTlv173(t173: ByteArray) {
        t173.read {
            val type = readByte()
            val host = readUShortLVString()
            val port = readShort()

            bot.logger.warning("服务器: host=$host, port=$port, type=$type")
            // SEE oicq_request.java at method analysisT173
        }
    }

    /**
     * ipv6 address
     */
    fun QQAndroidClient.analysisTlv17f(t17f: ByteArray) {
        t17f.read {
            val type = readByte()
            val host = readUShortLVString()
            val port = readShort()

            bot.logger.warning("服务器 ipv6: host=$host, port=$port, type=$type")
            // SEE oicq_request.java at method analysisT17f
        }
    }

    fun QQAndroidClient.analyzeTlv106(t106: ByteArray) {
        val tgtgtKey = decodeA1(t106) {
            discardExact(51)
            readBytes(16)
        }
        this.tgtgtKey = tgtgtKey
    }

    fun Input.readUShortLVString(): String = String(this.readUShortLVByteArray())
}

internal inline fun <R> QQAndroidClient.decodeA1(a1: ByteArray, block: ByteReadPacket.() -> R): R {
    val key = (account.passwordMd5 + ByteArray(4) + uin.toInt().toByteArray()).md5()
    val v = TEA.decrypt(a1, key)
    return v.toReadPacket().withUse(block)
}

internal fun ByteArray?.orEmpty(size: Int = 0): ByteArray {
    return this ?: if (size == 0) EMPTY_BYTE_ARRAY else ByteArray(size)
}