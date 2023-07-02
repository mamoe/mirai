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
import net.mamoe.mirai.internal.network.protocol.packet.createChannelProxy
import net.mamoe.mirai.internal.spi.EncryptServiceContext
import net.mamoe.mirai.internal.utils.actualCacheDir
import net.mamoe.mirai.internal.utils.crypto.QQEcdh
import net.mamoe.mirai.internal.utils.crypto.QQEcdhInitialPublicKey
import net.mamoe.mirai.internal.utils.crypto.verify
import net.mamoe.mirai.internal.utils.workingDirPath
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.buildTypeSafeMap
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.time.Duration.Companion.seconds


/**
 * Updater for updating [QQEcdhInitialPublicKey].
 */
internal interface EcdhInitialPublicKeyUpdater {
    /**
     * Refresh the [QQEcdhInitialPublicKey]
     */
    suspend fun refreshInitialPublicKeyAndApplyEcdh()

    suspend fun initializeSsoSecureEcdh()

    fun getQQEcdh(): QQEcdh

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

    var qqEcdh: QQEcdh? = null
    override fun getQQEcdh(): QQEcdh {
        if (qqEcdh == null) {
            error("Calling getQQEcdh without calling refreshInitialPublicKeyAndApplyEcdh")
        }
        return qqEcdh!!
    }

    override suspend fun refreshInitialPublicKeyAndApplyEcdh() {

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
                val resp = Json.decodeFromString(ServerRespPOJO.serializer(), respStr)
                resp.pubKeyMeta.let { meta ->
                    val key = QQEcdhInitialPublicKey(meta.keyVer, meta.pubKey, currentTimeSeconds() + resp.querySpan)
                    check(key.verify(meta.pubKeySign)) { "Ecdh public key which from server is invalid" }
                    logger.info("Successfully fetched ecdh public key from server.")
                    key
                }
            }
        }.getOrElse {
            logger.error("Failed to fetch ECDH public key from server, using default key instead", it)
            QQEcdhInitialPublicKey.default
        }
        bot.client.ecdhInitialPublicKey = initialPublicKey
        qqEcdh = QQEcdh(initialPublicKey)
    }

    override suspend fun initializeSsoSecureEcdh() {
        val encryptWorker = bot.encryptServiceOrNull

        if (encryptWorker == null) {
            logger.info("EncryptService SPI is not provided, sso secure ecdh will not be initialized.")
            return
        }

        encryptWorker.initialize(EncryptServiceContext(bot.id, buildTypeSafeMap {
            set(EncryptServiceContext.KEY_CHANNEL_PROXY, createChannelProxy(bot))
            set(EncryptServiceContext.KEY_DEVICE_INFO, bot.client.device)
            set(EncryptServiceContext.KEY_BOT_PROTOCOL, bot.configuration.protocol)
            set(EncryptServiceContext.KEY_QIMEI36, bot.client.qimei36 ?: "")
            set(EncryptServiceContext.KEY_BOT_WORKING_DIR, bot.configuration.workingDirPath)
            set(EncryptServiceContext.KEY_BOT_CACHING_DIR, bot.configuration.actualCacheDir().absolutePath)
        }))
    }


}
