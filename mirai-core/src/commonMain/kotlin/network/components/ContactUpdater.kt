/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.FriendImpl
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.StrangerImpl
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.GroupInfoImpl
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.contact.info.StrangerInfoImpl
import net.mamoe.mirai.internal.contact.toMiraiFriendInfo
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.isValid
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopNum
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcRespRegister
import net.mamoe.mirai.internal.network.protocol.data.jce.isValid
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.network.protocol.packet.list.StrangerList
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.retryCatching
import net.mamoe.mirai.utils.verbose

/**
 * Manager of caches for [Contact]s.
 *
 * Uses [ContactCacheService].
 */
internal interface ContactUpdater {
    val otherClientsLock: Mutex
    val groupListModifyLock: Mutex

    /**
     * Load all caches to the bot this [ContactUpdater] works for.
     *
     * Implementation must be thread-safe.
     */
    suspend fun loadAll(registerResp: SvcRespRegister)

    /**
     * Closes all contacts and save them to cache if needed.
     *
     * Implementation must be thread-safe.
     */
    fun closeAllContacts(e: CancellationException)

    companion object : ComponentKey<ContactUpdater>
}

internal class ContactUpdaterImpl(
    val bot: QQAndroidBot, // not good
    val components: ComponentStorage,
    private val logger: MiraiLogger,
) : ContactUpdater {
    override val otherClientsLock: Mutex = Mutex()
    override val groupListModifyLock: Mutex = Mutex()
    private val cacheService get() = components[ContactCacheService]
    private val lock = Mutex()

    override suspend fun loadAll(registerResp: SvcRespRegister) {
        lock.withLock {
            coroutineScope {
                launch { reloadFriendList(registerResp) }
                launch { reloadGroupList() }
                launch { reloadStrangerList() }
            }
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
    private suspend fun reloadFriendList(registerResp: SvcRespRegister) {
        if (initFriendOk) {
            return
        }

        val friendListCache = cacheService.friendListCache

        fun updateCacheSeq(list: List<FriendInfoImpl>) {
            cacheService.friendListCache?.apply {
                friendListSeq = registerResp.iLargeSeq
                timeStamp = registerResp.timeStamp
                this.list = list
                cacheService.saveFriendCache()
            }
        }

        suspend fun refreshFriendList(): List<FriendInfoImpl> {
            logger.info { "Start loading friend list..." }
            val friendInfos = mutableListOf<FriendInfoImpl>()

            var count = 0
            var total: Short
            while (true) {
                val data = FriendList.GetFriendGroupList(
                    bot.client, count, 150, 0, 0
                ).sendAndExpect(bot, timeoutMillis = 5000, retry = 2)

                total = data.totalFriendCount

                for (jceInfo in data.friendList) {
                    friendInfos.add(jceInfo.toMiraiFriendInfo())
                }

                count += data.friendList.size
                logger.verbose { "Loading friend list: ${count}/${total}" }
                if (count >= total) break
            }
            logger.info { "Successfully loaded friend list: $count in total" }
            return friendInfos
        }

        val list = if (friendListCache?.isValid(registerResp) == true) {
            val list = friendListCache.list
            logger.info { "Loaded ${list.size} friends from local cache." }

            // For sync bot nick
            FriendList.GetFriendGroupList(
                bot.client, 0, 1, 0, 0
            ).sendAndExpect<Packet>(bot)

            list
        } else {
            refreshFriendList().also {
                updateCacheSeq(it)
            }
        }

        for (friendInfoImpl in list) {
            addFriendToBot(friendInfoImpl)
        }


        initFriendOk = true
    }

    private fun addFriendToBot(it: FriendInfo) =
        bot.friends.delegate.add(FriendImpl(bot, bot.coroutineContext, it))

    private suspend fun addGroupToBot(stTroopNum: StTroopNum) = stTroopNum.run {
        suspend fun refreshGroupMemberList(): Sequence<MemberInfo> {
            return Mirai.getRawGroupMemberList(
                bot,
                groupUin,
                groupCode,
                dwGroupOwnerUin
            )
        }

        val cache = cacheService.groupMemberListCaches?.get(groupCode)
        val members = if (cache != null) {
            if (cache.isValid(stTroopNum)) {
                cache.list.asSequence().also {
                    logger.info { "Loaded ${cache.list.size} members from local cache for group $groupName (${groupCode})" }
                }
            } else refreshGroupMemberList().also { sequence ->
                cache.troopMemberNumSeq = dwMemberNumSeq ?: 0
                cache.list = sequence.mapTo(ArrayList()) { it as MemberInfoImpl }
                cacheService.groupMemberListCaches!!.reportChanged(groupCode)
            }
        } else {
            refreshGroupMemberList()
        }

        bot.groups.delegate.add(
            GroupImpl(
                bot = bot,
                coroutineContext = bot.coroutineContext,
                id = groupCode,
                groupInfo = GroupInfoImpl(stTroopNum),
                members = members
            )
        )
    }

    private suspend fun reloadStrangerList() {
        if (initStrangerOk) {
            return
        }
        var currentCount = 0
        logger.info { "Start loading stranger list..." }
        val response = StrangerList.GetStrangerList(bot.client)
            .sendAndExpect(bot, timeoutMillis = 5000, retry = 2)

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

    private suspend fun reloadGroupList() {
        if (initGroupOk) {
            return
        }
        TroopManagement.GetTroopConfig(bot.client).sendAndExpect(bot)

        logger.info { "Start loading group list..." }
        val troopListData = FriendList.GetTroopListSimplify(bot.client)
            .sendAndExpect(bot, retry = 5)

        val semaphore = Semaphore(30)

        coroutineScope {
            troopListData.groups.forEach { group ->
                launch {
                    semaphore.withPermit {
                        retryCatching(5) { addGroupToBot(group) }.getOrThrow()
                    }
                }
            }
        }

        logger.info { "Successfully loaded group list: ${troopListData.groups.size} in total." }
        cacheService.groupMemberListCaches?.saveGroupCaches()
        initGroupOk = true
    }


}