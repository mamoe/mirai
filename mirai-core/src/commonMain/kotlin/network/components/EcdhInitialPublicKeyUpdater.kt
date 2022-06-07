/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.utils.crypto.ECDH
import net.mamoe.mirai.internal.utils.crypto.ECDHInitialPublicKey
import net.mamoe.mirai.internal.utils.crypto.ECDHWithPublicKey
import net.mamoe.mirai.internal.utils.crypto.defaultInitialPublicKey
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.time.Duration.Companion.seconds


/**
 * Updater for updating [ECDHInitialPublicKey].
 */
internal interface EcdhInitialPublicKeyUpdater {
    /**
     * Refresh the [ECDHInitialPublicKey]
     */
    suspend fun refreshInitialPublicKeyAndApplyECDH()

    fun getECDHWithPublicKey(): ECDHWithPublicKey

    companion object : ComponentKey<EcdhInitialPublicKeyUpdater>
}

internal class EcdhInitialPublicKeyUpdaterImpl(
    private val bot: QQAndroidBot,
    private val logger: MiraiLogger
) : EcdhInitialPublicKeyUpdater {
    @Serializable
    data class ServerRespPOJO(

        @SerialName("PubKeyMeta")
        val pubKeyMeta: PubKeyMeta,

        @SerialName("QuerySpan")
        val querySpan: Int = 0
    )

    @Serializable
    data class PubKeyMeta(

        @SerialName("PubKey")
        val pubKey: String,

        @SerialName("PubKeySign")
        val pubKeySign: String,

        @SerialName("KeyVer")
        val keyVer: Int
    )

    companion object {
        val json = Json {}
    }

    var ecdhWithPublicKey: ECDHWithPublicKey? = null
    override fun getECDHWithPublicKey(): ECDHWithPublicKey {
        if (ecdhWithPublicKey == null) {
            error("Calling getECDHWithPublicKey without calling refreshInitialPublicKeyAndApplyECDH")
        }
        return ecdhWithPublicKey!!
    }

    override suspend fun refreshInitialPublicKeyAndApplyECDH() {

        val initialPublicKey = kotlin.runCatching {
            val currentPublicKey = bot.client.ecdhInitialPublicKey
            if (currentPublicKey.expireTime > currentTimeSeconds()) {
                logger.info("ECDH key is valid.")
                currentPublicKey
            } else {
                logger.info("ECDH key is invalid, start to fetch ecdh public key from server.")
                val respStr =
                    withTimeout(10.seconds) {
                        bot.components[HttpClientProvider].getHttpClient()
                            .get("https://keyrotate.qq.com/rotate_key?cipher_suite_ver=305&uin=${bot.client.uin}")
                            .bodyAsText()
                    }
                val resp = json.decodeFromString(ServerRespPOJO.serializer(), respStr)
                resp.pubKeyMeta.let { meta ->
                    val isValid = ECDH.verifyPublicKey(
                        version = meta.keyVer,
                        publicKey = meta.pubKey,
                        publicKeySign = meta.pubKeySign
                    )
                    check(isValid) { "Ecdh public key which from server is invalid" }
                    logger.info("Successfully fetched ecdh public key from server.")
                    ECDHInitialPublicKey(meta.keyVer, meta.pubKey, currentTimeSeconds() + resp.querySpan)
                }
            }
        }.getOrElse {
            logger.error("Failed to fetch ECDH public key from server, using default key instead", it)
            defaultInitialPublicKey
        }
        bot.client.ecdhInitialPublicKey = initialPublicKey
        ecdhWithPublicKey = ECDHWithPublicKey(initialPublicKey)
    }


}
