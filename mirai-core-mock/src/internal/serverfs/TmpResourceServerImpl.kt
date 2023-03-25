/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.serverfs

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import net.mamoe.mirai.mock.resserver.MockServerFileDisk
import net.mamoe.mirai.mock.resserver.TmpResourceServer
import net.mamoe.mirai.utils.*
import java.net.ServerSocket
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

internal class TmpResourceServerImpl(
    override val storageRoot: Path,
    private val serverPort: Int,
    private val closeSystemOnShutdown: Boolean,
) : TmpResourceServer {
    var logger by lateinitMutableProperty {
        MiraiLogger.Factory.create(TmpResourceServerImpl::class.java, "TmpFsServer-${hashCode()}")
    }
    lateinit var server: NettyApplicationEngine

    private var _serverUri: URI by lateinitMutableProperty {
        URI.create("http://localhost:$serverPort")
    }
    override val serverUri: URI get() = _serverUri

    override val mockServerFileDisk: MockServerFileDisk by lazy {
        MockServerFileDiskImpl(storageRoot.resolve("tx-fs-disk"))
    }

    private var _isActive: Boolean = false
    override val isActive: Boolean get() = _isActive

    private val storage: Path = storageRoot.resolve("storage").mkdirsIfMissing()
    private val images: Path = storageRoot.resolve("images").mkdirsIfMissing()

    override suspend fun uploadResource(resource: ExternalResource): String {
        fun ByteArray.hex() = toUHexString(separator = "")

        resource.useAutoClose {
            val resourceId = "${resource.size}-${resource.sha1.hex()}-${resource.md5.hex()}"
            val locPath = storage.resolve(resourceId)
            if (locPath.isFile) return resourceId
            runBIO {
                locPath.outputStream().use { output ->
                    resource.inputStream().use { it.copyTo(output) }
                }
            }
            return resourceId
        }
    }

    override fun isImageUploaded(md5: ByteArray, size: Long): Boolean {
        val img = images.resolve(generateUUID(md5))
        if (img.exists()) {
            return Files.size(img) == size
        }
        return false
    }


    override suspend fun uploadResourceAsImage(resource: ExternalResource): URI {
        val imgId = generateUUID(resource.md5)
        val resId = uploadResource(resource)

        val imgPath = images.resolve(imgId)
        val storagePath = storage.resolve(resId).toAbsolutePath()

        if (imgPath.exists()) {
            return resolveImageUrl(imgId)
        }

        kotlin.runCatching {
            imgPath.createLinkPointingTo(storagePath)
        }.recoverCatchingSuppressed {
            imgPath.createSymbolicLinkPointingTo(storagePath)
        }.getOrThrow()

        return resolveImageUrl(imgId)
    }

    override fun resolveHttpUrl(resourceId: String): URI {
        return serverUri.resolve("storage/$resourceId")
    }

    override fun resolveImageUrl(imgId: String): URI {
        return serverUri.resolve("images/$imgId")
    }

    override suspend fun invalidateResource(resourceId: String) {
        storage.resolve(resourceId).deleteIfExists()
    }

    override fun resolveHttpUrlByPath(path: Path): URI {
        if (path.fileSystem !== storageRoot.fileSystem)
            throw UnsupportedOperationException("Cross file system linking is not supported now")
        val pt = path.toAbsolutePath().toString().replace('\\', '/')
        return serverUri.resolve(
            "abs/" + URLEncoder.encode(pt, "UTF-8")
        )
    }

    override fun startupServer() {
        val port = if (serverPort == 0) {
            ServerSocket(0).use { it.localPort }
        } else serverPort
        _serverUri = URI.create("http://127.0.0.1:$port/")
        logger.info { "Tmp Fs Server started: $serverUri" }

        val server = embeddedServer(Netty, environment = applicationEngineEnvironment {
            connector {
                this.host = "127.0.0.1"
                this.port = port
            }
            module {
                @Suppress("BlockingMethodInNonBlockingContext")
                intercept(ApplicationCallPipeline.Call) {
                    val req = URI.create(call.request.origin.uri).path.removePrefix("/")
                    val targetPath = if (req.startsWith("abs/")) {
                        storageRoot.fileSystem.getPath(URLDecoder.decode(req.substring(4), "UTF-8"))
                    } else {
                        storageRoot.resolve(req)
                    }
                    if (targetPath.exists()) {
                        call.respondOutputStream {
                            net.mamoe.mirai.utils.runBIO {
                                targetPath.inputStream().buffered().use { it.copyTo(this@respondOutputStream) }
                            }
                        }
                        return@intercept
                    }
                    if (req.startsWith("images/")) {
                        call.respondRedirect(
                            "http://gchat.qpic.cn/gchatpic_new/1145141919/0-0-${
                                req.substring(7)
                            }/0?term=2", false
                        )
                        return@intercept
                    }
                }
            }
        })
        this.server = server
        server.start(wait = false)
    }

    override fun close() {
        if (this::server.isInitialized) {
            server.stop(0, 0)
        }
        if (closeSystemOnShutdown) {
            storageRoot.fileSystem.close()
        }
    }
}

private fun Path.mkdirsIfMissing(): Path {
    if (!exists()) createDirectories()
    return this
}