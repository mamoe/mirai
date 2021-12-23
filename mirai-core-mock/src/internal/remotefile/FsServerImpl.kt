/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.remotefile

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.mamoe.mirai.mock.fsserver.TmpFsServer
import net.mamoe.mirai.utils.*
import java.io.IOException
import java.net.ServerSocket
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Files
import java.util.*
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

internal class FsServerImpl(
    override val fsSystem: FileSystem,
    val httpPort: Int,
) : TmpFsServer {
    var logger by lateinitMutableProperty { MiraiLogger.Factory.create(TmpFsServer::class.java, "TmpFsServer") }

    override lateinit var httpRoot: String
    lateinit var server: NettyApplicationEngine

    override suspend fun uploadFile(resource: ExternalResource): String {
        val fid = "${currentTimeMillis()}-${UUID.randomUUID()}"
        logger.info { "New file upload request, fid=$fid, res-md5=${resource.md5.toUHexString()}, res-size=${resource.size}" }
        resource.useAutoClose { res ->
            runBIO {
                fsSystem.getPath(fid).also {
                    it.createFile()
                }.outputStream().use { fso ->
                    res.inputStream().buffered().use { it.copyTo(fso) }
                }
            }
        }
        return fid
    }

    override suspend fun bindFile(id: String, path: String) = runBIO<Unit> {
        val target = fsSystem.getPath(path.removePrefix("/"))
        val source = fsSystem.getPath(id)

        logger.info { "Linking $source to $target" }

        target.mkParentDirs()
        try {
            Files.createLink(target, source)
            logger.info { "Linked  $source to $target by Files.createLink" }
        } catch (ignore: IOException) {
            ignore.printStackTrace(System.out)
            Files.copy(source, target)
            logger.info { "Linked  $source to $target by Files.copy" }
        }
    }

    override fun startup() {
        val port = if (httpPort == 0) {
            ServerSocket(0).use { it.localPort }
        } else {
            httpPort
        }
        httpRoot = "http://127.0.0.1:$port/"

        logger.info { "Tmp Fs Server started: $httpRoot" }

        val server = embeddedServer(Netty, environment = applicationEngineEnvironment {
            connector {
                this.host = "127.0.0.1"
                this.port = port
            }
            module {
                intercept(ApplicationCallPipeline.Call) {
                    val request = URI.create(call.request.origin.uri).path.removePrefix("/")
                    val path = fsSystem.getPath(request)
                    logger.verbose { "New http request: $request" }
                    if (path.exists()) {
                        call.respondOutputStream {
                            runBIO {
                                path.inputStream().buffered().use { it.copyTo(this) }
                            }
                        }
                        finish()
                        return@intercept
                    }
                    if (request.startsWith("image/")) { // Redirect non-exists images to TX Image Server
                        //         return "http://gchat.qpic.cn/gchatpic_new/${bot.id}/0-0-${
                        //            imageId.substring(1..36)
                        //                .replace("-", "")
                        //        }/0?term=2"
                        call.respondRedirect(
                            "http://gchat.qpic.cn/gchatpic_new/1145141919/0-0-${
                                request.removePrefix("image/")
                            }/0?term=2", false
                        )
                    }
                }
            }
        })
        this.server = server
        server.start(false)
    }

    override fun close() {
        if (this::server.isInitialized) {
            server.stop(0, 0)
        }
        fsSystem.close()
    }
}