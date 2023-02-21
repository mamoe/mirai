/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.auth

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.toByteArray

public interface BotAuthorization {
    public suspend fun authorize(
        authComponent: BotAuthComponent,
        bot: BotAuthInfo,
    ): BotAuthorizationResult

    public fun calculateSecretsKey(
        bot: BotAuthInfo,
    ): ByteArray {
        return bot.deviceInfo.guid + bot.id.toByteArray()
    }

    public companion object {
    }
}

@NotStableForInheritance
public interface BotAuthorizationResult

@NotStableForInheritance
public interface BotAuthInfo {
    public val id: Long
    public val deviceInfo: DeviceInfo
    public val configuration: BotConfiguration
}

@NotStableForInheritance
public interface BotAuthComponent {
    public suspend fun authByPassword(password: String): BotAuthorizationResult
    public suspend fun authByPassword(passwordMd5: ByteArray): BotAuthorizationResult
    public suspend fun authByQRCode(): BotAuthorizationResult
}
