/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.txfs

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.mock.internal.remotefile.FsServerImpl
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.plusHttpSubpath
import java.io.Closeable
import java.nio.file.FileSystem
import java.nio.file.Path

/**
 * 临时 HTTP 文件中转服务器
 *
 * 该服务器用于中转在测试单元中上传的各种资源 (如图片, 语音 等)
 *
 * implementation note:
 *
 * 为了压缩内存占用与端口占用, 图片资源会直接链接到此服务器来搜索访问图片
 *
 * 在访问的 url 以 `/image/` 开头时, 如果对应文件不存在, 应该重定向至 `gchat.qpic.cn`
 *
 * @see FsServerImpl
 */
@JvmBlockingBridge
public interface TmpFsServer : Closeable {
    public val httpRoot: String
    public val fsSystem: FileSystem
    public val fsDisk: TxFileDisk


    /**
     * 上传一个资源
     * @return resource id, 该 id 必须可以直接被 `fsSystem.getPath(id)` 所引用
     */
    public suspend fun uploadFile(resource: ExternalResource): String

    /**
     * 上传一个资源并返回该资源的下载地址
     * @see [uploadFile]
     */
    public suspend fun uploadFileAndGetUrl(resource: ExternalResource): String {
        return getHttpUrl(uploadFile(resource))
    }

    /**
     * 桥接 [id] 到 HTTP 锚点 [path]
     *
     * implementation note:
     *
     * 传入的 [path] 不会以 '/' 开头, 并且在调用此函数后, 框架会直接通过
     * `httpRoot + path` 来访问该资源, 用于图片上传
     */
    public suspend fun bindFile(id: String, path: String)

    public fun startup()

    /**
     * 通过 [id] 得到 http url
     */
    public fun getHttpUrl(id: String): String {
        return httpRoot.plusHttpSubpath(id)
    }

    public fun resolveHttpUrl(path: Path): String

    public companion object {
        @JvmStatic
        public fun ofFsSystem(fs: FileSystem, port: Int = 0): TmpFsServer {
            return FsServerImpl(fs, port)
        }

        @JvmStatic
        public fun newInMemoryFsServer(port: Int = 0): TmpFsServer {
            val fs = Jimfs.newFileSystem(
                Configuration.unix()
                    .toBuilder()
                    .setWorkingDirectory("/")
                    .build()
            )
            return FsServerImpl(fs, port)
        }
    }
}