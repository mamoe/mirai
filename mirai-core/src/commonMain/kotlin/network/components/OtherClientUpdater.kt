/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.contact.deviceName
import net.mamoe.mirai.contact.platform
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.createOtherClient
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info

internal interface OtherClientUpdater {

    suspend fun update()

    companion object : ComponentKey<OtherClientUpdater>
}

internal class OtherClientUpdaterImpl(
    private val bot: QQAndroidBot,
    private val context: ComponentStorage,
    private val logger: MiraiLogger,
) : OtherClientUpdater {

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    val otherClientList = ContactList<OtherClient>()

    private val lock = Mutex()

    override suspend fun update() = lock.withLock {
        val list = Mirai.getOnlineOtherClientsList(bot)
        bot.otherClients.delegate.clear()
        bot.otherClients.delegate.addAll(list.map { bot.createOtherClient(it) })
        if (bot.otherClients.isEmpty()) {
            logger.info { "No OtherClient online." }
        } else {
            logger.info {
                "Online OtherClients: " +
                        bot.otherClients.joinToString { "${it.deviceName}(${it.platform?.name ?: "unknown platform"})" }
            }
        }
    }

}