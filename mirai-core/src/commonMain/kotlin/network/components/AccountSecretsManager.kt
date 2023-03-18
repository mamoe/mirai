/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.network.LoginExtraData
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.getRandomByteArray
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.get_mpasswd
import net.mamoe.mirai.internal.utils.accountSecretsFile
import net.mamoe.mirai.internal.utils.crypto.QQEcdhInitialPublicKey
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.*
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile
import kotlin.random.Random

/**
 * For a [Bot].
 *
 * @see MemoryAccountSecretsManager
 * @see FileCacheAccountSecretsManager
 * @see CombinedAccountSecretsManager
 */
internal interface AccountSecretsManager : Cacheable {
    fun saveSecrets(account: BotAccount, secrets: AccountSecrets)
    fun getSecrets(account: BotAccount): AccountSecrets?
    override fun invalidate()

    companion object : ComponentKey<AccountSecretsManager>
}

/**
 * Secrets for authentication with server. (login)
 */
internal interface AccountSecrets {
    var wLoginSigInfoField: WLoginSigInfo?

    val wLoginSigInfoInitialized get() = wLoginSigInfoField != null
    var wLoginSigInfo: WLoginSigInfo
        get() = wLoginSigInfoField ?: error("wLoginSigInfoField is not yet initialized")
        set(value) {
            wLoginSigInfoField = value
        }

    /**
     * t537
     */
    var loginExtraData: MutableSet<LoginExtraData>

    var G: ByteArray // sigInfo[2]
    var dpwd: ByteArray
    var randSeed: ByteArray // t403

    /**
     * t108 时更新
     */
    var ksid: ByteArray

    var tgtgtKey: ByteArray
    val randomKey: ByteArray
    var ecdhInitialPublicKey: QQEcdhInitialPublicKey
}


@Serializable
internal data class AccountSecretsImpl(
    override var loginExtraData: MutableSet<LoginExtraData>,
    override var wLoginSigInfoField: WLoginSigInfo?,
    override var G: ByteArray,
    override var dpwd: ByteArray = get_mpasswd().toByteArray(),
    override var randSeed: ByteArray,
    override var ksid: ByteArray,
    override var tgtgtKey: ByteArray,
    override val randomKey: ByteArray,
    override var ecdhInitialPublicKey: QQEcdhInitialPublicKey,
) : AccountSecrets, ProtoBuf {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false

        if (loginExtraData != other.loginExtraData) return false
        if (wLoginSigInfoField != other.wLoginSigInfoField) return false
        if (!G.contentEquals(other.G)) return false
        if (!dpwd.contentEquals(other.dpwd)) return false
        if (!randSeed.contentEquals(other.randSeed)) return false
        if (!ksid.contentEquals(other.ksid)) return false
        if (!tgtgtKey.contentEquals(other.tgtgtKey)) return false
        if (!randomKey.contentEquals(other.randomKey)) return false
        if (ecdhInitialPublicKey != other.ecdhInitialPublicKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loginExtraData.hashCode()
        result = 31 * result + (wLoginSigInfoField?.hashCode() ?: 0)
        result = 31 * result + G.contentHashCode()
        result = 31 * result + dpwd.contentHashCode()
        result = 31 * result + randSeed.contentHashCode()
        result = 31 * result + ksid.contentHashCode()
        result = 31 * result + tgtgtKey.contentHashCode()
        result = 31 * result + randomKey.contentHashCode()
        result = 31 * result + ecdhInitialPublicKey.hashCode()
        return result
    }
}

internal fun AccountSecretsImpl(
    other: AccountSecrets,
): AccountSecretsImpl = other.run {
    AccountSecretsImpl(
        loginExtraData, wLoginSigInfoField, G, dpwd,
        randSeed, ksid, tgtgtKey, randomKey, ecdhInitialPublicKey
    )
}

internal fun AccountSecretsImpl(
    device: DeviceInfo,
): AccountSecretsImpl {
    return AccountSecretsImpl(
        loginExtraData = ConcurrentSet(),
        wLoginSigInfoField = null,
        G = device.guid,
        dpwd = get_mpasswd().toByteArray(),
        randSeed = EMPTY_BYTE_ARRAY,
        ksid = EMPTY_BYTE_ARRAY,
        tgtgtKey = (Random.nextBytes(16) + device.guid).md5(),
        randomKey = getRandomByteArray(16),
        ecdhInitialPublicKey = QQEcdhInitialPublicKey.default
    )
}


internal fun AccountSecretsManager.getSecretsOrCreate(account: BotAccount, device: DeviceInfo): AccountSecrets {
    var secrets = getSecrets(account)
    if (secrets == null) {
        secrets = AccountSecretsImpl(device)
        saveSecrets(account, secrets)
    }
    return secrets
}

internal class MemoryAccountSecretsManager : AccountSecretsManager {
    @Volatile
    private var instance: AccountSecrets? = null

    @Synchronized
    override fun saveSecrets(account: BotAccount, secrets: AccountSecrets) {
        instance = secrets
    }

    @Synchronized
    override fun getSecrets(account: BotAccount): AccountSecrets? = this.instance

    @Synchronized
    override fun invalidate() {
        instance = null
    }
}


internal class FileCacheAccountSecretsManager(
    val file: MiraiFile,
    val logger: MiraiLogger,
) : AccountSecretsManager {
    @Synchronized
    override fun saveSecrets(account: BotAccount, secrets: AccountSecrets) {
        if (secrets.wLoginSigInfoField == null) return
        saveSecretsToFile(file, account, secrets)
        logger.info { "Saved account secrets to local cache for fast login." }
    }

    @Synchronized
    override fun getSecrets(account: BotAccount): AccountSecrets? {
        return getSecretsImpl(account)
    }

    private fun getSecretsImpl(account: BotAccount): AccountSecrets? {
        if (!file.exists()) return null
        val loaded = kotlin.runCatching {
            TEA.decrypt(file.readBytes(), account.accountSecretsKey).loadAs(AccountSecretsImpl.serializer())
        }.getOrElse { e ->
            if (e.message == "Field 'ecdhInitialPublicKey' is required for type with serial name 'net.mamoe.mirai.internal.network.components.AccountSecretsImpl', but it was missing") {
                logger.info { "Detected old account secrets, invalidating..." }
            } else {
                logger.error("Failed to load account secrets from local cache. Invalidating cache...", e)
            }
            file.delete()
            return null
        }

        logger.info { "Loaded account secrets from local cache." }
        return loaded
    }

    @Synchronized
    override fun invalidate() {
        file.delete()
    }

    companion object {
        fun saveSecretsToFile(file: MiraiFile, account: BotAccount, secrets: AccountSecrets) {
            file.writeBytes(
                TEA.encrypt(
                    AccountSecretsImpl(secrets).toByteArray(AccountSecretsImpl.serializer()),
                    account.accountSecretsKey
                )
            )
        }
    }
}

internal class CombinedAccountSecretsManager(
    private val primary: AccountSecretsManager,
    private val alternative: AccountSecretsManager,
) : AccountSecretsManager {
    override fun saveSecrets(account: BotAccount, secrets: AccountSecrets) {
        primary.saveSecrets(account, secrets)
        alternative.saveSecrets(account, secrets)
    }

    override fun getSecrets(account: BotAccount): AccountSecrets? {
        return primary.getSecrets(account) ?: alternative.getSecrets(account)
    }

    override fun invalidate() {
        primary.invalidate()
        alternative.invalidate()
    }
}

/**
 * Create a [CombinedAccountSecretsManager] with [MemoryAccountSecretsManager] as primary and [FileCacheAccountSecretsManager] as an alternative.
 */
internal fun BotConfiguration.createAccountsSecretsManager(logger: MiraiLogger): AccountSecretsManager {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return if (accountSecrets) {
        CombinedAccountSecretsManager(
            MemoryAccountSecretsManager(),
            FileCacheAccountSecretsManager(accountSecretsFile(), logger)
        )
    } else {
        MemoryAccountSecretsManager()
    }
}
