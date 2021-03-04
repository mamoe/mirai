/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.FileMessage
import java.io.File

/**
 * @since 2.5
 */
@MiraiExperimentalApi
@JvmBlockingBridge
public interface RemoteFile {
    /**
     * 文件名或目录名.
     */
    public val name: String

    /**
     * 文件的 ID. 群文件允许重名, ID 非空时用来区分重名.
     */
    public val id: String?

    /**
     * 标准的绝对路径, 起始字符为 '/'.
     */
    public val path: String

    /**
     * 获取父目录, 当 [RemoteFile] 表示根目录时返回 `null`
     */
    public val parent: RemoteFile?

    /**
     * 当 [RemoteFile] 表示一个文件时返回 `true`.
     */
    public suspend fun isFile(): Boolean

    /**
     * 当 [RemoteFile] 表示一个目录时返回 `true`.
     */
    public suspend fun isDirectory(): Boolean = !isFile()

    /**
     * 获取文件长度. 当 [RemoteFile] 表示一个目录时行为不确定.
     */
    public suspend fun length(): Long

    public class FileInfo @MiraiInternalApi constructor(
        /**
         * 文件或目录名.
         */
        public val name: String,
        /**
         * 唯一识别标识.
         */
        public val id: String,
        /**
         * 标准绝对路径.
         */
        public val path: String,
        /**
         * 文件长度 (大小) bytes, 目录的 [length] 不确定.
         */
        public val length: Long,
        /**
         * 下载次数. 目录没有下载次数, 此属性总是 `0`.
         */
        public val downloadTimes: Int,
        /**
         * 上传者 ID. 目录没有上传者, 此属性总是 `0`.
         */
        public val uploaderId: Long,
        /**
         * 上传的时间. 目录没有上传时间, 此属性总是 `0`.
         */
        public val uploadTime: Long,
        /**
         * 上次修改时间.
         */
        public val lastModifyTime: Long,
        public val sha1: ByteArray,
        public val md5: ByteArray,
    ) {
        /**
         * 根据 [FileInfo.id] 或 [FileInfo.path] 获取到对应的 [RemoteFile].
         */
        public suspend fun resolveToFile(group: Group): RemoteFile =
            group.filesRoot.resolveById(id) ?: group.filesRoot.resolve(path)
    }

    /**
     * 获取这个文件或目录此时的信息. 当文件或目录不存在时返回 `null`.
     */
    public suspend fun getInfo(): FileInfo?

    /**
     * 当文件或目录存在时返回 `true`.
     */
    public suspend fun exists(): Boolean

    /**
     * 获取该目录下所有文件, 返回的 [RemoteFile] 都拥有 [RemoteFile.id] 用于区分重名文件或目录. 当 [RemoteFile] 表示一个文件时返回 [emptyFlow].
     */
    public suspend fun listFiles(): Flow<RemoteFile>

    /**
     * 获取该目录下所有文件, 返回的 [RemoteFile] 都拥有 [RemoteFile.id] 用于区分重名文件或目录. 当 [RemoteFile] 表示一个文件时返回空迭代器.
     * @param lazy 为 `true` 时惰性获取, 为 `false` 时立即获取全部文件列表.
     */
    @JavaFriendlyAPI
    public suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile>

    /**
     * 获取该目录的子文件. 不会检查 [RemoteFile] 是否表示一个目录.
     *
     * @param relative 当初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolve stdlib 内的类似函数
     */
    public fun resolve(relative: String): RemoteFile

    /**
     * 获取该目录的子文件. 不会检查 [RemoteFile] 是否表示一个目录. 返回的 [RemoteFile.id] 将会与 `relative.id` 相同.
     *
     * @param relative 当 [RemoteFile.path] 初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolve stdlib 内的类似函数
     */
    public fun resolve(relative: RemoteFile): RemoteFile

    /**
     * 获取该目录下的 ID 为 [id] 的文件, 当 [deep] 为 `true` 时还会进入子目录继续寻找这样的文件. 在不存在时返回 `null`.
     * @see resolve
     */
    public suspend fun resolveById(id: String, deep: Boolean = true): RemoteFile?

    /**
     * 获取该目录或子目录下的 ID 为 [id] 的文件, 在不存在时返回 `null`
     * @see resolve
     */
    public suspend fun resolveById(id: String): RemoteFile? = resolveById(id, deep = true)

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
     * 不会检查 [RemoteFile] 是否表示一个目录. 返回的 [RemoteFile.id] 将会与 `relative.id` 相同.
     *
     * @param relative 当 [RemoteFile.path] 初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolveSibling stdlib 内的类似函数
     */
    public fun resolveSibling(relative: RemoteFile): RemoteFile

    /**
     * 删除这个文件或目录. 若目录非空, 则会删除目录中的所有文件. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时返回 `false`.
     */
    public suspend fun delete(): Boolean

    /**
     * 重命名这个文件或目录, 将会更改 [RemoteFile.name] 属性值.
     * 操作非 Bot 自己上传的文件时需要管理员权限.
     */
    public suspend fun renameTo(name: String): Boolean

    /**
     * 将这个目录或文件移动到另一个位置. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时返回 `false`.
     */
    public suspend fun moveTo(target: RemoteFile): Boolean

    /**
     * 将这个目录或文件移动到另一个位置. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时返回 `false`.
     */
    public suspend fun moveTo(path: String): Boolean

    /**
     * 创建目录. 目录已经存在或无管理员权限时返回 `false`.
     */
    public suspend fun mkdir(): Boolean

    /**
     * 向这个文件上传数据. 当 [RemoteFile] 表示一个目录时抛出 [IllegalStateException]. 上传后不会关闭 [resource].
     *
     * 若 [RemoteFile.id] 存在且旧文件存在, 将会覆盖旧文件.
     * 即使用 [resolve] 或 [resolveSibling] 获取到的 [RemoteFile] 的 [upload] 总是上传一个新文件,
     * 而使用 [resolveById] 或 [listFiles] 获取到的总是覆盖旧文件, 当旧文件已在远程删除时上传一个新文件.
     */
    public suspend fun upload(resource: ExternalResource): Boolean

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

    /**
     * 得到相应文件消息, 可以发送. 当 [RemoteFile] 表示一个目录或文件不存在时返回 `null`.
     */
    public suspend fun toMessage(): FileMessage?

    public class DownloadInfo @MiraiInternalApi constructor(
        /**
         * @see RemoteFile.name
         */
        public val filename: String,
        /**
         * @see RemoteFile.id
         */
        public val id: String,
        /**
         * 标准绝对路径
         * @see RemoteFile.path
         */
        public val path: String,
        /**
         * HTTP or HTTPS URL
         */
        public val url: String,
        public val sha1: ByteArray,
        public val md5: ByteArray,
    ) {
        override fun toString(): String {
            return "DownloadInfo(filename='$filename', path='$path', url='$url', sha1=${sha1.toUHexString("")}, " +
                    "md5=${md5.toUHexString("")})"
        }
    }
}