/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.flow.Flow
import net.mamoe.kjbb.JvmBlockingBridge
import java.io.File

/**
 * @since 2.5
 */
@MiraiExperimentalApi
@JvmBlockingBridge
public interface RemoteFile {
    /**
     * 文件名或目录名
     */
    public val name: String

    /**
     * 标准的绝对路径, 起始字符为 '/'.
     */
    public val path: String

    /**
     * 获取父目录, 当 [RemoteFile] 表示根目录时返回 `null`
     */
    public val parent: RemoteFile?

    /**
     * 当 [RemoteFile] 表示一个文件时返回 `true`
     */
    public suspend fun isFile(): Boolean

    /**
     * 当 [RemoteFile] 表示一个目录时返回 `true`
     */
    public suspend fun isDirectory(): Boolean = !isFile()

    /**
     * 获取文件长度. 当 [RemoteFile] 表示一个目录时行为不确定
     */
    public suspend fun length(): Long

    /**
     * 当文件或目录存在时返回 `true`
     */
    public suspend fun exists(): Boolean

    /**
     * 获取该目录下所有文件. 当 [RemoteFile] 表示一个目录时返回 `null`
     */
    public suspend fun listFiles(): Flow<RemoteFile>?

    /**
     * 获取该目录下所有文件. 当 [RemoteFile] 表示一个目录时返回 `null`
     */
    @JavaFriendlyAPI
    public suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile>?

    /**
     * 获取该目录的子文件. 不会检查 [RemoteFile] 是否表示一个目录.
     *
     * @param relative 当初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolve stdlib 内的类似函数
     */
    public fun resolve(relative: String): RemoteFile

    /**
     * 获取该目录的子文件. 不会检查 [RemoteFile] 是否表示一个目录.
     *
     * @param relative 当 [RemoteFile.path] 初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolve stdlib 内的类似函数
     */
    public fun resolve(relative: RemoteFile): RemoteFile = resolve(relative.path)

    /**
     * 获取父目录的子文件. 如 `RemoteFile("/foo/bar").resolveSibling("gav")` 为 `RemoteFile("/foo/gav")`.
     * 不会检查 [RemoteFile] 是否表示一个目录.
     *
     * @param relative 当初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolveSibling stdlib 内的类似函数
     */
    public fun resolveSibling(relative: String): RemoteFile

    /**
     * 获取父目录的子文件. 如 `RemoteFile("/foo/bar").resolveSibling("gav")` 为 `RemoteFile("/foo/gav")`.
     * 不会检查 [RemoteFile] 是否表示一个目录.
     *
     * @param relative 当 [RemoteFile.path] 初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolveSibling stdlib 内的类似函数
     */
    public fun resolveSibling(relative: RemoteFile): RemoteFile = resolve(relative.path)

    /**
     * 删除这个文件或目录. 当 [recursively] 为 `true` 时会递归删除目录下所有子文件和目录, 否则将不会删除非空目录.
     */
    public suspend fun delete(recursively: Boolean): Boolean

    /**
     * 将这个目录或文件移动到另一个位置.
     */
    public suspend fun moveTo(target: RemoteFile): Boolean

    /**
     * 下载这个文件, 并将文件上传到另一个位置.
     */
    @MiraiExperimentalApi
    public suspend fun copyTo(target: RemoteFile): Boolean

    /**
     * 向这个文件上传数据并覆盖原文件内容. 当 [RemoteFile] 表示一个目录时抛出 [IllegalStateException]
     */
    public suspend fun write(resource: ExternalResource, override: Boolean = false): Boolean

//    /**
//     * 打开一个异步文件上传会话, 向这个文件上传数据并覆盖原文件内容. 当 [RemoteFile] 表示一个目录时抛出 [IllegalStateException]
//     * @see write 相当于带回调的异步 [write]
//     */
//    public suspend fun writeSession(resource: ExternalResource): FileUploadSession

    /**
     * 获取文件下载链接. 当 [RemoteFile] 表示一个目录时抛出 [IllegalStateException]
     */
    public suspend fun getDownloadInfo(): DownloadInfo

    /**
     * @return [path]
     */
    public override fun toString(): String

    public class DownloadInfo(
        /**
         * @see RemoteFile.name
         */
        public val filename: String,
        /**
         * 标准绝对路径
         * @see RemoteFile.path
         */
        public val path: String,
        /**
         * HTTP or HTTPS URL
         */
        public val url: String,
        //  public val cookie: String,
        public val sha: ByteArray,
//        public val sha3: ByteArray,
        public val md5: ByteArray,
    ) {
        override fun toString(): String {
            return "DownloadInfo(filename='$filename', path='$path', url='$url', sha=${sha.toUHexString("")}, " +
                    "md5=${md5.toUHexString("")})"
        }
    }
}