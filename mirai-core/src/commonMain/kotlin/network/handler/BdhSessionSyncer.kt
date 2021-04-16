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
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.JsonForCache
import net.mamoe.mirai.internal.network.ProtoBufForCache
import net.mamoe.mirai.internal.utils.actualCacheDir
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet

@Serializable
private data class ServerHostAndPort(
    val host: String,
    val port: Int,
)

private val ServerListSerializer: KSerializer<List<ServerHostAndPort>> =
    ListSerializer(ServerHostAndPort.serializer())

@OptIn(ExperimentalCoroutinesApi::class)
internal class BdhSessionSyncer(
    private val bot: QQAndroidBot
) {
    var bdhSession: CompletableDeferred<BdhSession> = CompletableDeferred()
    val hasSession: Boolean
        get() = kotlin.runCatching { bdhSession.getCompleted() }.isSuccess

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
        get() = bot.configuration.actualCacheDir().resolve("session.bin")
    private val serverListCacheFile: File
        get() = bot.configuration.actualCacheDir().resolve("servers.json")

    fun loadServerListFromCache() {
        val serverListCacheFile = this.serverListCacheFile
        if (serverListCacheFile.isFile) {
            bot.network.logger.verbose("Loading server list from cache.")
            kotlin.runCatching {
                val list = JsonForCache.decodeFromString(ServerListSerializer, serverListCacheFile.readText())
                bot.serverList.clear()
                bot.serverList.addAll(list.map { it.host to it.port })
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
                    ProtoBufForCache.decodeFromByteArray(BdhSession.serializer(), sessionCacheFile.readBytes()),
                    doSave = false
                )
            }.onFailure {
                kotlin.runCatching { sessionCacheFile.delete() }
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
                    bot.serverList.map { ServerHostAndPort(it.first, it.second) }
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
                sessionCacheFile.writeBytes(
                    ProtoBufForCache.encodeToByteArray(
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

@Serializable
internal class BdhSession(
    val sigSession: ByteArray,
    val sessionKey: ByteArray,
    var ssoAddresses: MutableSet<Pair<Int, Int>> = CopyOnWriteArraySet(),
    var otherAddresses: MutableSet<Pair<Int, Int>> = CopyOnWriteArraySet(),
)