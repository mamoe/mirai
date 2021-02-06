/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import contact.StrangerImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.FriendImpl
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.info.GroupInfoImpl
import net.mamoe.mirai.internal.contact.info.StrangerInfoImpl
import net.mamoe.mirai.internal.contact.toMiraiFriendInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopNum
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.list.StrangerList
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.retryCatching
import net.mamoe.mirai.utils.verbose

internal interface ContactCache {
}

internal interface ContactUpdater {
    suspend fun loadAll()

    fun closeAllContacts(e: CancellationException)
}

internal class ContactUpdaterImpl(
    val bot: QQAndroidBot,
) : ContactUpdater {
    @Synchronized
    override suspend fun loadAll() {
        coroutineScope {
            launch { reloadFriendList() }
            launch { reloadGroupList() }
            launch { reloadStrangerList() }
        }
    }

    @Synchronized
    override fun closeAllContacts(e: CancellationException) {
        if (!initFriendOk) {
            bot.friends.delegate.removeAll { it.cancel(e); true }
        }
        if (!initGroupOk) {
            bot.groups.delegate.removeAll { it.cancel(e); true }
        }
        if (!initStrangerOk) {
            bot.strangers.delegate.removeAll { it.cancel(e); true }
        }
    }


    @Volatile
    private var initFriendOk = false

    @Volatile
    private var initGroupOk = false

    @Volatile
    private var initStrangerOk = false

    /**
     * Don't use concurrently
     */
    private suspend fun reloadFriendList() = bot.network.run {
        if (initFriendOk) {
            return
        }

        logger.info { "Start loading friend list..." }
        var currentFriendCount = 0
        var totalFriendCount: Short
        while (true) {
            val data = FriendList.GetFriendGroupList(
                bot.client, currentFriendCount, 150, 0, 0
            ).sendAndExpect<FriendList.GetFriendGroupList.Response>(timeoutMillis = 5000, retry = 2)

            totalFriendCount = data.totalFriendCount
            data.friendList.forEach {
                // atomic
                bot.friends.delegate.add(
                    FriendImpl(bot, bot.coroutineContext, it.toMiraiFriendInfo())
                ).also { currentFriendCount++ }
            }
            logger.verbose { "Loading friend list: ${currentFriendCount}/${totalFriendCount}" }
            if (currentFriendCount >= totalFriendCount) {
                break
            }
            // delay(200)
        }
        logger.info { "Successfully loaded friend list: $currentFriendCount in total" }
        initFriendOk = true
    }

    private suspend fun StTroopNum.reloadGroup() {
        bot.groups.delegate.add(
            GroupImpl(
                bot = bot,
                coroutineContext = bot.coroutineContext,
                id = groupCode,
                groupInfo = GroupInfoImpl(this),
                members = Mirai.getRawGroupMemberList(
                    bot,
                    groupUin,
                    groupCode,
                    dwGroupOwnerUin
                )
            )
        )
    }

    private suspend fun reloadStrangerList() = bot.network.run {
        if (initStrangerOk) {
            return
        }
        var currentCount = 0
        logger.info { "Start loading stranger list..." }
        val response = StrangerList.GetStrangerList(bot.client)
            .sendAndExpect<StrangerList.GetStrangerList.Response>(timeoutMillis = 5000, retry = 2)

        if (response.result == 0) {
            response.strangerList.forEach {
                // atomic
                bot.strangers.delegate.add(
                    StrangerImpl(bot, bot.coroutineContext, StrangerInfoImpl(it.uin, it.nick.decodeToString()))
                ).also { currentCount++ }
            }
        }
        logger.info { "Successfully loaded stranger list: $currentCount in total" }
        initStrangerOk = true

    }

    private suspend fun reloadGroupList() = bot.network.run {
        if (initGroupOk) {
            return
        }
        logger.info { "Start syncing group config..." }
        TroopManagement.GetTroopConfig(bot.client).sendAndExpect<TroopManagement.GetTroopConfig.Response>()
        logger.info { "Successfully synced group config." }

        logger.info { "Start loading group list..." }
        val troopListData = FriendList.GetTroopListSimplify(bot.client)
            .sendAndExpect<FriendList.GetTroopListSimplify.Response>(retry = 5)

        val semaphore = Semaphore(30)

        coroutineScope {
            troopListData.groups.forEach { group ->
                launch {
                    semaphore.withPermit {
                        retryCatching(5) { group.reloadGroup() }.getOrThrow()
                    }
                }
            }
        }
        logger.info { "Successfully loaded group list: ${troopListData.groups.size} in total." }
        initGroupOk = true
    }


}