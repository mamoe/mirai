/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.BotClientHolder
import net.mamoe.mirai.internal.network.components.HttpClientProvider
import net.mamoe.mirai.internal.network.components.SsoProcessorContext
import net.mamoe.mirai.internal.network.protocol
import net.mamoe.mirai.internal.utils.crypto.aesDecrypt
import net.mamoe.mirai.internal.utils.crypto.aesEncrypt
import net.mamoe.mirai.internal.utils.crypto.rsaEncryptWithX509PubKey
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import kotlin.random.Random

private val secret = "ZdJqM15EeO2zWc08"
private val rsaPubKey = """
-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEIxgwoutfwoJxcGQeedgP7FG9
qaIuS0qzfR8gWkrkTZKM2iWHn2ajQpBRZjMSoSf6+KJGvar2ORhBfpDXyVtZCKpq
LQ+FLkpncClKVIrBwv6PHyUvuCb0rIarmgDnzkfQAqVufEtR64iazGDKatvJ9y6B
9NMbHddGSAUmRTCrHQIDAQAB
-----END PUBLIC KEY-----
""".trimIndent()

internal suspend fun QQAndroidBot.requestQimei(logger: MiraiLogger) {
    if (configuration.protocol != MiraiProtocol.ANDROID_PAD && configuration.protocol != MiraiProtocol.ANDROID_PHONE)
        return

    val protocol = components[BotClientHolder].client.protocol
    val deviceInfo = components[SsoProcessorContext].device
    val httpClient = components[HttpClientProvider].getHttpClient()

    val seed = deviceInfo.guid.foldRight(0x6f4L) { curr, acc -> acc + curr.toLong() }
    val random = Random(seed)

    val reservedData = Json.encodeToString(
        ReservedData(
            harmony = "0",
            clone = "0",
            containe = "",
            oz = "UhYmelwouA+V2nPWbOvLTgN2/m8jwGB+yUB5v9tysQg=",
            oo = "Xecjt+9S1+f8Pz2VLSxgpw==",
            kelong = "0",
            uptimes = formatTime(currentTimeMillis() - random.nextLong(14_400_000), null),
            multiUser = "0",
            bod = deviceInfo.board.decodeToString(),
            brd = deviceInfo.brand.decodeToString(),
            dv = deviceInfo.device.decodeToString(),
            firstLevel = "",
            manufact = deviceInfo.brand.decodeToString(),
            name = deviceInfo.model.decodeToString(),
            host = "se.infra",
            kernel = deviceInfo.procVersion.decodeToString(),
        )
    )

    val yearMonthFormatted = formatTime(currentTimeMillis(), "yyyy-MM-01")
    val rand1 = random.nextInt(899999) + 100000
    val rand2 = random.nextInt(899999999) + 100000000

    val beaconId = buildString {
        (1..40).forEach { i ->
            when (i) {
                1, 2, 13, 14, 17, 18, 21, 22, 25, 26, 29, 30, 33, 34, 37, 38 -> {
                    append('k')
                    append(i)
                    append(':')
                    append(yearMonthFormatted)
                    append(rand1)
                    append('.')
                    append(rand2)
                }

                3 -> append("k3:0000000000000000")
                4 -> {
                    append("k4:")
                    append(getRandomString(16))
                }

                else -> {
                    append('k')
                    append(i)
                    append(':')
                    append(random.nextInt(10000))
                }
            }
            append(';')
        }
    }

    val payloadParam = Json.encodeToString(
        DevicePayloadData(
            androidId = deviceInfo.androidId.decodeToString(),
            platformId = 1,
            appKey = "0S200MNJT807V3GE", // TODO: move to MiraiProtocolInternal
            appVersion = protocol.buildVer,
            beaconIdSrc = beaconId,
            brand = deviceInfo.brand.decodeToString(),
            channelId = "2017",
            cid = "",
            imei = deviceInfo.imei,
            imsi = "",
            mac = deviceInfo.macAddress.decodeToString(),
            model = deviceInfo.model.decodeToString(),
            networkType = "unknown",
            oaid = "",
            osVersion = buildString {
                append("Android ")
                append(deviceInfo.version.release.toString())
                append(", level ")
                append(deviceInfo.version.sdk.toString())
            },
            qimei = "",
            qimei36 = "",
            sdkVersion = "1.2.13.6",
            audit = "",
            userId = "{}",
            packageId = protocol.apkId,
            deviceType = if (configuration.protocol == MiraiProtocol.ANDROID_PAD) "Pad" else "Phone",
            sdkName = "",
            reserved = reservedData,
        )
    ).toByteArray()

    val aesKey = getRandomString(16).toByteArray()
    val nonce = getRandomString(16)
    val timestamp = currentTimeSeconds() * 1000

    val encodedAESKey = rsaEncryptWithX509PubKey(aesKey, rsaPubKey.encodeToByteArray(), timestamp).encodeBase64()
    val encodedPayloadParam = aesEncrypt(payloadParam, aesKey, aesKey).encodeBase64()

    val payload = Json.encodeToString(
        PostData(
            key = encodedAESKey,
            params = encodedPayloadParam,
            time = timestamp,
            nonce = nonce,
            sign = buildString {
                append(encodedAESKey)
                append(encodedPayloadParam)
                append(timestamp)
                append(nonce)
                append(secret)
            }.md5().toUHexString(""),
            extra = ""
        )
    )

    val resp = Json.decodeFromString<OLAAndroidResp>(
        httpClient.post("https://snowflake.qq.com/ola/android") {
            userAgent("Dalvik/2.1.0 (Linux; U; Android 7.1.2; PCRT00 Build/N2G48H)")
            contentType(ContentType.Application.Json)
            header("Cookie", "")
            setBody(payload.toByteArray())
            timeout {
                connectTimeoutMillis = 5000
                requestTimeoutMillis = 5000
                socketTimeoutMillis = 5000
            }
        }.bodyAsText()
    )

    if (resp.code != 0) {
        logger.warning { "Cannot get qimei from server, return code = ${resp.code}" }
        return
    }

    val decryptedData = aesDecrypt(resp.data.decodeBase64(), aesKey, aesKey)
    val qimeiData = Json.decodeFromString<QimeiData>(decryptedData.decodeToString())

    client.device.qimei36 = qimeiData.q36
    client.device.qimei16 = qimeiData.q16
}

@Serializable
private data class OLAAndroidResp(
    val code: Int,
    val data: String,
)

@Serializable
private data class QimeiData(
    val q16: String,
    val q36: String,
)

@Serializable
private data class ReservedData(
    val harmony: String,
    val clone: String,
    val containe: String,
    val oz: String,
    val oo: String,
    val kelong: String,
    val uptimes: String,
    val multiUser: String,
    val bod: String,
    val brd: String,
    val dv: String,
    val firstLevel: String,
    val manufact: String,
    val name: String,
    val host: String,
    val kernel: String
)

@Serializable
private data class DevicePayloadData(
    val androidId: String,
    val platformId: Int,
    val appKey: String,
    val appVersion: String,
    val beaconIdSrc: String,
    val brand: String,
    val channelId: String,
    val cid: String,
    val imei: String,
    val imsi: String,
    val mac: String,
    val model: String,
    val networkType: String,
    val oaid: String,
    val osVersion: String,
    val qimei: String,
    val qimei36: String,
    val sdkVersion: String,
    val audit: String,
    val userId: String,
    val packageId: String,
    val deviceType: String,
    val sdkName: String,
    val reserved: String
)

@Serializable
private data class PostData(
    val key: String,
    val params: String,
    val time: Long,
    val nonce: String,
    val sign: String,
    val extra: String
)