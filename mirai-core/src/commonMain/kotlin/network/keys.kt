/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUShort
import net.mamoe.mirai.internal.network.getRandomByteArray
import net.mamoe.mirai.internal.network.protocol.packet.PacketLogger
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.NoServerAvailableException
import net.mamoe.mirai.utils.*


internal class ReserveUinInfo(
    val imgType: ByteArray,
    val imgFormat: ByteArray,
    val imgUrl: ByteArray
) {
    override fun toString(): String {
        return "ReserveUinInfo(imgType=${imgType.toUHexString()}, imgFormat=${imgFormat.toUHexString()}, imgUrl=${imgUrl.toUHexString()})"
    }
}

internal class WFastLoginInfo(
    val outA1: ByteReadPacket,
    var adUrl: String = "",
    var iconUrl: String = "",
    var profileUrl: String = "",
    var userJson: String = ""
) {
    override fun toString(): String {
        return "WFastLoginInfo(outA1=$outA1, adUrl='$adUrl', iconUrl='$iconUrl', profileUrl='$profileUrl', userJson='$userJson')"
    }
}

internal class WLoginSimpleInfo(
    val uin: Long, // uin
    val imgType: ByteArray,
    val imgFormat: ByteArray,
    val imgUrl: ByteArray,
    val mainDisplayName: ByteArray
) {
    override fun toString(): String {
        return "WLoginSimpleInfo(uin=$uin, imgType=${imgType.toUHexString()}, imgFormat=${imgFormat.toUHexString()}, imgUrl=${imgUrl.toUHexString()}, mainDisplayName=${mainDisplayName.toUHexString()})"
    }
}

internal class LoginExtraData(
    val uin: Long,
    val ip: ByteArray,
    val time: Int,
    val version: Int
) {
    override fun toString(): String {
        return "LoginExtraData(uin=$uin, ip=${ip.toUHexString()}, time=$time, version=$version)"
    }
}

internal class WLoginSigInfo(
    val uin: Long,
    val encryptA1: ByteArray?, // sigInfo[0]
    /**
     * WARNING, please check [QQAndroidClient.tlv16a]
     */
    val noPicSig: ByteArray?, // sigInfo[1]

    val simpleInfo: WLoginSimpleInfo,

    var appPri: Long,
    var a2ExpiryTime: Long,
    var loginBitmap: Long,
    var tgt: ByteArray,
    var a2CreationTime: Long,
    var tgtKey: ByteArray,
    var userStSig: UserStSig,
    /**
     * TransEmpPacket 加密使用
     */
    var userStKey: ByteArray,
    var userStWebSig: UserStWebSig,
    var userA5: UserA5,
    var userA8: UserA8,
    var lsKey: LSKey,
    var sKey: SKey,
    var userSig64: UserSig64,
    var openId: ByteArray,
    var openKey: OpenKey,
    var vKey: VKey,
    var accessToken: AccessToken,
    var d2: D2,
    var d2Key: ByteArray,
    var sid: Sid,
    var aqSig: AqSig,
    var psKeyMap: PSKeyMap,
    var pt4TokenMap: Pt4TokenMap,
    var superKey: ByteArray,
    var payToken: ByteArray,
    var pf: ByteArray,
    var pfKey: ByteArray,
    var da2: ByteArray,
    // val pt4Token: ByteArray,
    var wtSessionTicket: WtSessionTicket,
    var wtSessionTicketKey: ByteArray,
    var deviceToken: ByteArray
) {
    override fun toString(): String {
        return "WLoginSigInfo(uin=$uin, encryptA1=${encryptA1?.toUHexString()}, noPicSig=${noPicSig?.toUHexString()}, simpleInfo=$simpleInfo, appPri=$appPri, a2ExpiryTime=$a2ExpiryTime, loginBitmap=$loginBitmap, tgt=${tgt.toUHexString()}, a2CreationTime=$a2CreationTime, tgtKey=${tgtKey.toUHexString()}, userStSig=$userStSig, userStKey=${userStKey.toUHexString()}, userStWebSig=$userStWebSig, userA5=$userA5, userA8=$userA8, lsKey=$lsKey, sKey=$sKey, userSig64=$userSig64, openId=${openId.toUHexString()}, openKey=$openKey, vKey=$vKey, accessToken=$accessToken, d2=$d2, d2Key=${d2Key.toUHexString()}, sid=$sid, aqSig=$aqSig, psKey=$psKeyMap, superKey=${superKey.toUHexString()}, payToken=${payToken.toUHexString()}, pf=${pf.toUHexString()}, pfKey=${pfKey.toUHexString()}, da2=${da2.toUHexString()}, wtSessionTicket=$wtSessionTicket, wtSessionTicketKey=${wtSessionTicketKey.toUHexString()}, deviceToken=${deviceToken.toUHexString()})"
    }
}

internal class UserStSig(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class LSKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserStWebSig(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserA8(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserA5(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class SKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserSig64(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class OpenKey(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class VKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class AccessToken(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class D2(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
internal class Sid(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class AqSig(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)

internal class Pt4Token(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal typealias PSKeyMap = MutableMap<String, PSKey>
internal typealias Pt4TokenMap = MutableMap<String, Pt4Token>

internal fun Input.readUShortLVString(): String = kotlinx.io.core.String(this.readUShortLVByteArray())

internal fun Input.readUShortLVByteArray(): ByteArray = this.readBytes(this.readUShort().toInt())

internal fun parsePSKeyMapAndPt4TokenMap(
    data: ByteArray,
    creationTime: Long,
    expireTime: Long,
    outPSKeyMap: PSKeyMap,
    outPt4TokenMap: Pt4TokenMap
) =
    data.read {
        repeat(readShort().toInt()) {
            val domain = readUShortLVString()
            val psKey = readUShortLVByteArray()
            val pt4token = readUShortLVByteArray()

            when {
                psKey.isNotEmpty() -> outPSKeyMap[domain] = PSKey(psKey, creationTime, expireTime)
                pt4token.isNotEmpty() -> outPt4TokenMap[domain] = Pt4Token(pt4token, creationTime, expireTime)
            }
        }
    }

internal class PSKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class WtSessionTicket(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)

internal open class KeyWithExpiry(
    data: ByteArray,
    creationTime: Long,
    val expireTime: Long
) : KeyWithCreationTime(data, creationTime) {
    override fun toString(): String {
        return "KeyWithExpiry(data=${data.toUHexString()}, creationTime=$creationTime)"
    }
}

internal open class KeyWithCreationTime(
    val data: ByteArray,
    val creationTime: Long
) {
    override fun toString(): String {
        return "KeyWithCreationTime(data=${data.toUHexString()}, creationTime=$creationTime)"
    }
}

internal suspend inline fun QQAndroidClient.useNextServers(crossinline block: suspend (host: String, port: Int) -> Unit) {
    if (bot.serverList.isEmpty()) {
        bot.bdhSyncer.loadServerListFromCache()
        if (bot.serverList.isEmpty()) {
            bot.serverList.addAll(DefaultServerList)
        }
    }
    retryCatchingExceptions(bot.serverList.size, except = LoginFailedException::class) l@{
        val pair = bot.serverList[0]
        runCatchingExceptions {
            block(pair.first, pair.second)
            return@l
        }.getOrElse {
            bot.serverList.remove(pair)
            if (it !is LoginFailedException) {
                // 不要重复打印.
                bot.logger.warning(it)
            }
            throw it
        }
    }.getOrElse {
        if (it is LoginFailedException) {
            throw it
        }
        bot.serverList.addAll(DefaultServerList)
        throw NoServerAvailableException(it)
    }
}


@Suppress("RemoveRedundantQualifierName") // bug
internal fun generateTgtgtKey(guid: ByteArray): ByteArray =
    (getRandomByteArray(16) + guid).md5()

internal inline fun <R> QQAndroidClient.tryDecryptOrNull(
    data: ByteArray,
    size: Int = data.size,
    mapper: (ByteArray) -> R
): R? {
    keys.forEach { (key, value) ->
        kotlin.runCatching {
            return mapper(TEA.decrypt(data, value, size).also { PacketLogger.verbose { "成功使用 $key 解密" } })
        }
    }
    return null
}

internal fun QQAndroidClient.allKeys() = mapOf(
    "16 zero" to ByteArray(16),
    "D2 key" to wLoginSigInfo.d2Key,
    "wtSessionTicketKey" to wLoginSigInfo.wtSessionTicketKey,
    "userStKey" to wLoginSigInfo.userStKey,
    "tgtgtKey" to tgtgtKey,
    "tgtKey" to wLoginSigInfo.tgtKey,
    "deviceToken" to wLoginSigInfo.deviceToken,
    "shareKeyCalculatedByConstPubKey" to ecdh.keyPair.initialShareKey
    //"t108" to wLoginSigInfo.t1,
    //"t10c" to t10c,
    //"t163" to t163
)
