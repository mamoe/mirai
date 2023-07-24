/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.qimei

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
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
    val protocol = components[BotClientHolder].client.protocol
    if (protocol.appKey.isEmpty()) return

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
            appKey = protocol.appKey,
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
                append(deviceInfo.version.release.decodeToString())
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

    val encodedAESKey = rsaEncryptWithX509PubKey(aesKey, rsaPubKey, timestamp).encodeBase64()
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

    val resp = Json.decodeFromString(
        OLAAndroidResp.serializer(),
        httpClient.post("https://snowflake.qq.com/ola/android") {
            userAgent(buildString {
                append("Dalvik/")
                append(dalvikVersions[deviceInfo.version.sdk] ?: "2.1.0")
                append(" (Linux; U; Android ")
                append(deviceInfo.version.release.decodeToString())
                append("; ")
                append(deviceInfo.device.decodeToString())
                append(" Build/")
                append(deviceInfo.display.decodeToString())
                append(")")
            })
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
    val qimeiData = Json.decodeFromString(QimeiData.serializer(), decryptedData.decodeToString())

    client.bot.components[SsoProcessorContext].let { context ->
        context.qimei36 = qimeiData.q36
        context.qimei16 = qimeiData.q16
    }
}

private val dalvikVersions = mapOf(
    14 to "1.6",
    15 to "1.6",
    16 to "1.6",
    17 to "1.6",
    18 to "1.6",
    19 to "2.0",
    20 to "2.0",
    21 to "2.1.0",
    22 to "2.1.0",
    23 to "2.1.0",
    24 to "2.1.0",
    25 to "2.1.0",
    26 to "2.1.0",
    27 to "2.1.0",
    28 to "2.1.0",
    29 to "2.1.0",
    30 to "2.1.0",
    31 to "2.1.0",
    32 to "2.1.0",
    33 to "2.1.0",
    34 to "2.1.0",
)

@Serializable
private class OLAAndroidResp(
    val code: Int,
    val data: String,
)

@Serializable
private class QimeiData(
    val q16: String,
    val q36: String,
)

@Suppress("unused")
@Serializable
private class ReservedData(
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

@Suppress("unused")
@Serializable
private class DevicePayloadData(
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

@Suppress("unused")
@Serializable
private class PostData(
    val key: String,
    val params: String,
    val time: Long,
    val nonce: String,
    val sign: String,
    val extra: String
)
