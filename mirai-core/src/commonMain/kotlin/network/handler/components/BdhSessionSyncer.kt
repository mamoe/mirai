/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import net.mamoe.mirai.internal.network.JsonForCache
import net.mamoe.mirai.internal.network.ProtoBufForCache
import net.mamoe.mirai.internal.network.handler.context.BdhSession
import net.mamoe.mirai.internal.utils.actualCacheDir
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File

private val ServerListSerializer: KSerializer<Set<ServerAddress>> =
    SetSerializer(ServerAddress.serializer())

@OptIn(ExperimentalCoroutinesApi::class)
internal class BdhSessionSyncer(
    private val configuration: BotConfiguration,
    private val serverList: ServerList,
    private val logger: MiraiLogger,
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
        get() = configuration.actualCacheDir().resolve("session.bin")
    private val serverListCacheFile: File
        get() = configuration.actualCacheDir().resolve("servers.json")

    fun loadServerListFromCache() {
        val serverListCacheFile = this.serverListCacheFile
        if (serverListCacheFile.isFile) {
            logger.verbose("Loading server list from cache.")
            kotlin.runCatching {
                val list = JsonForCache.decodeFromString(ServerListSerializer, serverListCacheFile.readText())
                serverList.setPreferred(list.map { ServerAddress(it.host, it.port) })
            }.onFailure {
                logger.warning("Error in loading server list from cache", it)
            }
        } else {
            logger.verbose("No server list cached.")
        }
    }

    fun loadFromCache() {
        val sessionCacheFile = this.sessionCacheFile
        if (sessionCacheFile.isFile) {
            logger.verbose("Loading BdhSession from cache file")
            kotlin.runCatching {
                overrideSession(
                    ProtoBufForCache.decodeFromByteArray(BdhSession.serializer(), sessionCacheFile.readBytes()),
                    doSave = false
                )
            }.onFailure {
                kotlin.runCatching { sessionCacheFile.delete() }
                logger.warning("Error in loading BdhSession from cache", it)
            }
        } else {
            logger.verbose("No BdhSession cache")
        }
    }

    fun saveServerListToCache() {
        val serverListCacheFile = this.serverListCacheFile
        serverListCacheFile.parentFile?.mkdirs()

        logger.verbose("Saving server list to cache")
        kotlin.runCatching {
            serverListCacheFile.writeText(
                JsonForCache.encodeToString(
                    ServerListSerializer,
                    serverList.getPreferred()
                )
            )
        }.onFailure {
            logger.warning("Error in saving ServerList to cache.", it)
        }
    }

    fun saveToCache() {
        val sessionCacheFile = this.sessionCacheFile
        sessionCacheFile.parentFile?.mkdirs()
        if (bdhSession.isCompleted) {
            logger.verbose("Saving bdh session to cache")
            kotlin.runCatching {
                sessionCacheFile.writeBytes(
                    ProtoBufForCache.encodeToByteArray(
                        BdhSession.serializer(),
                        bdhSession.getCompleted()
                    )
                )
            }.onFailure {
                logger.warning("Error in saving BdhSession to cache.", it)
            }
        } else {
            sessionCacheFile.delete()
            logger.verbose("No BdhSession to save to cache")
        }

    }
}

