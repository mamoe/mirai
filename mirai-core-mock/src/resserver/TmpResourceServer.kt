/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.resserver

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.internal.serverfs.TmpResourceServerImpl
import net.mamoe.mirai.utils.ExternalResource
import java.io.Closeable
import java.net.URI
import java.nio.file.Path

/**
 * 临时资源中转服务器
 *
 * 此服务器用于中转测试中涉及到的各种临时数据, 如 图片、语音、群文件 等
 *
 * 如果 [TmpResourceServer] 被用于 [MockBot], 在 [MockBot] 关闭时也会同步关闭 [TmpResourceServer]
 *
 */
@JvmBlockingBridge
public interface TmpResourceServer : Closeable {
    public val serverUri: URI
    public val storageRoot: Path
    public val mockServerFileDisk: MockServerFileDisk
    public val isActive: Boolean

    /**
     * 上传一个资源
     *
     * @return 资源 ID, 可通过 [resolveHttpUrl] 获得 http 链接
     */
    public suspend fun uploadResource(resource: ExternalResource): String

    /**
     * 上传图片
     *
     * @return 图片的 http 链接
     */
    public suspend fun uploadResourceAsImage(resource: ExternalResource): URI

    /**
     * 通过图片 md5 和 size 判断图片是否已经上传
     */
    public fun isImageUploaded(md5: ByteArray, size: Long): Boolean
    public suspend fun uploadResourceAndGetUrl(resource: ExternalResource): String {
        return resolveHttpUrl(uploadResource(resource)).toString()
    }

    public fun resolveHttpUrl(resourceId: String): URI
    public fun resolveImageUrl(imgId: String): URI

    /**
     * 立即释放目标资源, 此后再次访问该资源 ([resourceId]) 时会得到 404 Not Found
     */
    public suspend fun invalidateResource(resourceId: String)

    /**
     * 获取一个对应 [path] 的 http 链接
     */
    public fun resolveHttpUrlByPath(path: Path): URI

    /**
     * 启动 Http Server.
     *
     * 如果 [TmpResourceServer] 被用于 [MockBot], [MockBot] 会自动启动服务器, 请不要自行启动
     */
    public fun startupServer()

    public companion object {
        @JvmStatic
        public fun of(
            path: Path,
            port: Int = 0,
            closeFileSystemWhenClose: Boolean = false,
        ): TmpResourceServer {
            return TmpResourceServerImpl(path, port, closeFileSystemWhenClose)
        }

        @JvmStatic
        public fun newInMemoryTmpResourceServer(port: Int = 0): TmpResourceServer {
            val fs = Jimfs.newFileSystem(
                Configuration.unix()
                    .toBuilder()
                    .setWorkingDirectory("/")
                    .build()
            )
            return of(fs.getPath("/"), port, true)
        }
    }
}
