/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopNum
import net.mamoe.mirai.internal.utils.ScheduledJob
import net.mamoe.mirai.internal.utils.groupCacheDir
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.createFileIfNotExists
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.runBIO
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

internal val JsonForCache = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}

internal val ProtoBufForCache = ProtoBuf {
    encodeDefaults = true
}

@Serializable
internal data class FriendListCache(
    var friendListSeq: Long = 0,
    /**
     * 实际上是个序列号, 不是时间
     */
    var timeStamp: Long = 0,
    var list: List<FriendInfoImpl> = emptyList(),
)

@Serializable
internal data class GroupMemberListCache(
    var troopMemberNumSeq: Long,
    var list: List<MemberInfoImpl> = emptyList(),
)

internal fun GroupMemberListCache.isValid(stTroopNum: StTroopNum): Boolean {
    return this.list.size == stTroopNum.dwMemberNum?.toInt() && this.troopMemberNumSeq == stTroopNum.dwMemberNumSeq
}

internal class GroupMemberListCaches(
    private val bot: QQAndroidBot,
    private val logger: MiraiLogger,
) {
    init {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        bot.eventChannel.parentScope(bot)
            .subscribeAlways<net.mamoe.mirai.event.events.BaseGroupMemberInfoChangeEvent> {
                groupListSaver.notice()
            }

    }

    private val changedGroups: MutableCollection<Long> = ConcurrentLinkedQueue()
    private val groupListSaver: ScheduledJob by lazy {
        ScheduledJob(bot.coroutineContext, bot.configuration.contactListCache.saveIntervalMillis) {
            runBIO { saveGroupCaches() }
        }
    }

    fun reportChanged(groupCode: Long) {
        changedGroups.add(groupCode)
        groupListSaver.notice()
    }

    private fun takeCurrentChangedGroups(): Map<Long, GroupMemberListCache> {
        val ret = HashMap<Long, GroupMemberListCache>()
        changedGroups.removeIf {
            ret[it] = get(it)
            true
        }
        return ret
    }

    private val cacheDir: File by lazy { bot.configuration.groupCacheDir() }

    private fun resolveCacheFile(groupCode: Long): File {
        cacheDir.mkdirs()
        return cacheDir.resolve("$groupCode.json")
    }

    fun saveGroupCaches() {
        val currentChanged = takeCurrentChangedGroups()
        if (currentChanged.isNotEmpty()) {
            for ((id, cache) in currentChanged) {
                val file = resolveCacheFile(id)
                file.createFileIfNotExists()
                file.writeText(JsonForCache.encodeToString(GroupMemberListCache.serializer(), cache))
            }
            logger.info { "Saved ${currentChanged.size} groups to local cache." }
        }
    }

    val map: MutableMap<Long, GroupMemberListCache> = ConcurrentHashMap()

    fun retainAll(list: Collection<Long>) {
        this.map.keys.retainAll(list)
    }

    operator fun get(id: Long): GroupMemberListCache {
        return map.getOrPut(id) {
            val file = resolveCacheFile(id)
            if (file.exists() && file.isFile) {
                val text = file.readText()
                if (text.isNotBlank()) {
                    return JsonForCache.decodeFromString(GroupMemberListCache.serializer(), text)
                }
            }

            GroupMemberListCache(0, emptyList())
        }
    }
}