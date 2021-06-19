/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.FriendListCache
import net.mamoe.mirai.internal.network.GroupMemberListCaches
import net.mamoe.mirai.internal.network.JsonForCache
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.utils.ScheduledJob
import net.mamoe.mirai.internal.utils.friendCacheFile
import net.mamoe.mirai.utils.*

/**
 * Strategy of caching contacts. Used by [ContactUpdater].
 */
internal interface ContactCacheService {
    val friendListCache: FriendListCache? // from old code
    val groupMemberListCaches: GroupMemberListCaches? // from old code

    /**
     * Implementation does not need to be thread-safe.
     */
    fun saveFriendCache() // from old code

    companion object : ComponentKey<ContactCacheService>
}

internal class ContactCacheServiceImpl(
    private val bot: QQAndroidBot,
    private val logger: MiraiLogger,
) : ContactCacheService {
    private val configuration get() = bot.configuration

    ///////////////////////////////////////////////////////////////////////////
    // contact cache
    ///////////////////////////////////////////////////////////////////////////

    inline val json get() = configuration.json

    override val friendListCache: FriendListCache? by lazy {
        if (!configuration.contactListCache.friendListCacheEnabled) return@lazy null
        val file = configuration.friendCacheFile()
        val ret = file.loadNotBlankAs(FriendListCache.serializer(), JsonForCache) ?: FriendListCache()

        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        bot.eventChannel.parentScope(bot)
            .subscribeAlways<net.mamoe.mirai.event.events.FriendInfoChangeEvent> {
                friendListSaver?.notice()
            }
        ret
    }

    override val groupMemberListCaches: GroupMemberListCaches? by lazy {
        if (!configuration.contactListCache.groupMemberListCacheEnabled) {
            return@lazy null
        }
        GroupMemberListCaches(bot, logger)
    }

    private val friendListSaver: ScheduledJob? by lazy {
        if (!configuration.contactListCache.friendListCacheEnabled) return@lazy null
        ScheduledJob(bot.coroutineContext, configuration.contactListCache.saveIntervalMillis) {
            runBIO { saveFriendCache() }
        }
    }

    override fun saveFriendCache() {
        val friendListCache = friendListCache ?: return

        configuration.friendCacheFile().run {
            createFileIfNotExists()
            writeText(JsonForCache.encodeToString(FriendListCache.serializer(), friendListCache))
            logger.info { "Saved ${friendListCache.list.size} friends to local cache." }
        }
    }

}