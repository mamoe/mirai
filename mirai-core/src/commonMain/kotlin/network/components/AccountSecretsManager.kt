/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.context.AccountSecrets
import net.mamoe.mirai.internal.network.context.AccountSecretsImpl
import net.mamoe.mirai.internal.utils.actualCacheDir
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import java.io.File

/**
 * For a [Bot].
 *
 * @see MemoryAccountSecretsManager
 * @see FileCacheAccountSecretsManager
 * @see CombinedAccountSecretsManager
 */
internal interface AccountSecretsManager {
    fun saveSecrets(account: BotAccount, secrets: AccountSecrets)
    fun getSecrets(account: BotAccount): AccountSecrets?

    companion object : ComponentKey<BdhSessionSyncer>
}

internal fun AccountSecretsManager.getSecretsOrCreate(account: BotAccount, device: DeviceInfo): AccountSecrets {
    var secrets = getSecrets(account)
    if (secrets == null) {
        secrets = AccountSecretsImpl(device, account)
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
}


internal class FileCacheAccountSecretsManager(
    val file: File,
    val logger: MiraiLogger,
) : AccountSecretsManager {
    override fun saveSecrets(account: BotAccount, secrets: AccountSecrets) {
        if (secrets.wLoginSigInfoField == null) return

        file.writeBytes(
            TEA.encrypt(
                AccountSecretsImpl(secrets).toByteArray(AccountSecretsImpl.serializer()),
                account.passwordMd5
            )
        )

        logger.info { "Saved account secrets to local cache for fast login." }
    }

    override fun getSecrets(account: BotAccount): AccountSecrets? {
        return getSecretsImpl(account)
    }

    private fun getSecretsImpl(account: BotAccount): AccountSecrets? {
        if (!file.exists()) return null
        val loaded = kotlin.runCatching {
            TEA.decrypt(file.readBytes(), account.passwordMd5).loadAs(AccountSecretsImpl.serializer())
        }.getOrElse { e ->
            logger.error("Failed to load account secrets from local cache. Invalidating cache...", e)
            file.delete()
            return null
        }

        logger.info { "Loaded account secrets from local cache." }
        return loaded
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
}

/**
 * Create a [CombinedAccountSecretsManager] with [MemoryAccountSecretsManager] as primary and [FileCacheAccountSecretsManager] as an alternative.
 */
internal fun BotConfiguration.createAccountsSecretsManager(logger: MiraiLogger): AccountSecretsManager {
    return CombinedAccountSecretsManager(
        MemoryAccountSecretsManager(),
        FileCacheAccountSecretsManager(
            actualCacheDir().resolve("account.secrets"),
            logger
        )
    )
}
