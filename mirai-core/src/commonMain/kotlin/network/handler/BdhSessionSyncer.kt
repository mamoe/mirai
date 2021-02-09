/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.BdhSession
import net.mamoe.mirai.internal.network.JsonForCache
import java.io.File

private val ServerListSerializer: KSerializer<List<Pair<String, Int>>> =
    ListSerializer(PairSerializer(String.serializer(), Int.serializer()))

@OptIn(ExperimentalCoroutinesApi::class)
internal class BdhSessionSyncer(
    private val bot: QQAndroidBot
) {
    var bdhSession: CompletableDeferred<BdhSession> = CompletableDeferred()
    val hasSession: Boolean get() = bdhSession.isCompleted

    fun overrideSession(
        session: BdhSession,
        doSave: Boolean = true
    ) {
        bdhSession.complete(session)
        bdhSession = CompletableDeferred(session)
        if (doSave) {
            saveToCache()
        }
    }

    private val sessionCacheFile: File
        get() = bot.configuration.cacheDirSupplier().resolve("session.json")
    private val serverListCacheFile: File
        get() = bot.configuration.cacheDirSupplier().resolve("serverlist.json")

    fun loadServerListFromCache() {
        val serverListCacheFile = this.serverListCacheFile
        if (serverListCacheFile.isFile) {
            bot.network.logger.verbose("Loading server list from cache.")
            kotlin.runCatching {
                val list = JsonForCache.decodeFromString(ServerListSerializer, serverListCacheFile.readText())
                bot.serverList.clear()
                bot.serverList.addAll(list)
            }.onFailure {
                bot.network.logger.warning("Error in loading server list from cache", it)
            }
        } else {
            bot.network.logger.verbose("No server list cached.")
        }
    }

    fun loadFromCache() {
        val sessionCacheFile = this.sessionCacheFile
        if (sessionCacheFile.isFile) {
            bot.network.logger.verbose("Loading BdhSession from cache file")
            kotlin.runCatching {
                overrideSession(
                    JsonForCache.decodeFromString(BdhSession.serializer(), sessionCacheFile.readText()),
                    doSave = false
                )
            }.onFailure {
                bot.network.logger.warning("Error in loading BdhSession from cache", it)
            }
        } else {
            bot.network.logger.verbose("No BdhSession cache")
        }
    }

    fun saveServerListToCache() {
        val serverListCacheFile = this.serverListCacheFile
        serverListCacheFile.parentFile?.mkdirs()

        bot.network.logger.verbose("Saving server list to cache")
        kotlin.runCatching {
            serverListCacheFile.writeText(
                JsonForCache.encodeToString(
                    ServerListSerializer,
                    bot.serverList
                )
            )
        }.onFailure {
            bot.network.logger.warning("Error in saving ServerList to cache.", it)
        }
    }

    fun saveToCache() {
        val sessionCacheFile = this.sessionCacheFile
        sessionCacheFile.parentFile?.mkdirs()
        if (bdhSession.isCompleted) {
            bot.network.logger.verbose("Saving bdh session to cache")
            kotlin.runCatching {
                sessionCacheFile.writeText(
                    JsonForCache.encodeToString(
                        BdhSession.serializer(),
                        bdhSession.getCompleted()
                    )
                )
            }.onFailure {
                bot.network.logger.warning("Error in saving BdhSession to cache.", it)
            }
        } else {
            sessionCacheFile.delete()
            bot.network.logger.verbose("No BdhSession to save to cache")
        }

    }
}
