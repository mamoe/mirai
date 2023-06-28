/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.network.client
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.spi.EncryptService
import net.mamoe.mirai.internal.spi.EncryptServiceContext
import net.mamoe.mirai.internal.spi.GlobalEncryptServiceUsage
import net.mamoe.mirai.utils.buildTypeSafeMap

internal interface EncryptServiceHolder {
    companion object : ComponentKey<EncryptServiceHolder>

    val isAvailable: Boolean

    val service: EncryptService

    val serviceOrNull: EncryptService?

}


internal class EncryptServiceHolderImpl(
    bot: AbstractBot,
) : EncryptServiceHolder {
    override var isAvailable: Boolean = false
        private set

    private var service0: EncryptService? = null

    override val serviceOrNull: EncryptService? get() = service0
    override val service: EncryptService
        get() = service0 ?: error("Encrypt Service not available")

    init {
        @OptIn(GlobalEncryptServiceUsage::class)
        EncryptService.instance?.let { globalService ->
            service0 = globalService.attachToBot(
                EncryptServiceContext(bot.id, buildTypeSafeMap {
                    set(EncryptServiceContext.KEY_BOT_PROTOCOL, bot.configuration.protocol)
                    set(EncryptServiceContext.KEY_DEVICE_INFO, bot.client.device)
                })
            )
            isAvailable = true
        }
    }
}


internal val AbstractBot.encryptService: EncryptService get() = components[EncryptServiceHolder].service
internal val AbstractBot.encryptServiceOrNull: EncryptService? get() = components[EncryptServiceHolder].serviceOrNull
