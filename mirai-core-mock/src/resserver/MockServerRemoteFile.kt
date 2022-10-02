/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.resserver

import net.mamoe.mirai.utils.ExternalResource
import java.nio.file.Path

public interface MockServerRemoteFile {
    public val system: MockServerFileSystem

    public val isFile: Boolean
    public val isDirectory: Boolean
    public val name: String
    public val path: String
    public val id: String
    public val exists: Boolean
    public val parent: MockServerRemoteFile
    public val size: Long

    public fun listFiles(): Sequence<MockServerRemoteFile>?
    public fun delete(): Boolean
    public fun rename(name: String): Boolean

    /**
     * 移动文件
     * @param path 目标目录
     */
    public fun moveTo(path: MockServerRemoteFile)

    public fun asExternalResource(): ExternalResource
    public fun resolveNativePath(): Path

    public fun uploadFile(name: String, content: ExternalResource, uploader: Long): MockServerRemoteFile

    public fun mksubdir(name: String, creator: Long): MockServerRemoteFile

    public var fileInfo: TxRemoteFileInfo
}

public data class TxRemoteFileInfo(
    @JvmField var creator: Long,
    @JvmField var createTime: Long,
    @JvmField var lastUpdateTime: Long,
)
