/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")
@file:JvmBlockingBridge

package net.mamoe.mirai.utils

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.RemoteFile.Companion.uploadFile
import net.mamoe.mirai.utils.RemoteFile.ProgressionCallback.Companion.asProgressionCallback
import java.io.File

/**
 * 表示一个远程文件或目录.
 *
 * [RemoteFile] 仅保存 [id], [name], [path], [parent], [contact] 这五个属性, 除获取这些属性外的所有的操作都是在*远程*完成的.
 * 意味着操作的结果会因文件或目录在服务器中的状态变化而变化.
 *
 * 与 [File] 类似, [RemoteFile] 是不可变的. [renameTo] 和 [copyTo] 会操作远程文件, 但不会修改当前 [RemoteFile.path] 等属性.
 *
 * ## 文件操作
 *
 * 所有文件操作都在 [RemoteFile] 对象中完成. 可通过 [FileSupported.filesRoot] 获取到表示根目录路径的 [RemoteFile], 并通过 [resolve] 获取到其内文件.
 *
 * 示例:
 * ```
 * val file1: RemoteFile = group.filesRoot.resolve("/foo.txt") // 获取表示群文件 "foo.txt" 的 RemoteFile 实例
 * val file2: RemoteFile = group.filesRoot.resolve("/dir/foo.txt") // 获取表示群文件目录 "dir" 中的 "foo.txt" 的 RemoteFile 实例
 *
 *
 * val downloadInfo = file1.getDownloadInfo() // 获取该文件的下载方式, 可以自行下载
 *
 *
 * val message: FileMessage = file2.upload(resource) // 向路径 "/dir/foo.txt" 上传一个文件, 返回可以发送到群内的文件消息.
 * group.sendMessage(message) // 发送文件消息到群, 用户才会收到机器人上传文件的提醒. 可以多次发送.
 *
 * file2.uploadAndSend(resource) // 上传文件并发送文件消息. 是上面两行的简单版本.
 *
 *
 * // 要直接上传文件, 也可以简单地使用任一:
 * group.uploadFile("/foo.txt", resource) // Kotlin
 * resource.uploadAsFileTo(group, "/foo.txt") // Kotlin
 * FileSupported.uploadFile(group, "/foo.txt", resource"); // Java
 * ExternalResource.uploadAsFile(resource, group, "/foo.txt") // Java
 * ```
 *
 * ## 目录操作
 * [RemoteFile] 类似于 [java.io.File], 也可以表示一个目录.
 * ```
 * val dir: RemoteFile = group.filesRoot.resolve("/foo") // 获取表示目录 "foo" 的 RemoteFile 实例
 *
 * if (dir.exists()) { // 判断目录是否存在
 *   // ...
 * }
 *
 * dir.listFiles() // Kotlin 使用, 获取该目录中的文件列表.
 * dir.listFilesIterator() // Java 使用, 获取该目录中的文件列表.
 * ```
 *
 * 注意, 服务器目前只支持一层目录. 即只能存在 "/foo.txt" 和 "/xxx/foo.txt", 而 "/xxx/xxx/foo.txt" 不受支持.
 *
 * ## 文件名和目录名可重复
 *
 * 服务器允许相同名称的文件或目录存在, 这就导致 "/foo" 可能表示多个重名文件中的一个, 也可能表示一个目录. 依靠路径的判断因此不可靠.
 *
 * 这个特性带来的行为有:
 * - [`FileSupported.uploadFile`][uploadFile] 总是往一个路径上传文件, 如果有同名文件存在, 不会覆盖, 而是再创建一个同名文件.
 * - [delete] 可能会删除重名文件中的任何一个, 也可能会删除一个目录, 操作顺序取决于服务器.
 *
 * 为了解决这个问题, [RemoteFile] 可以拥有一个由服务器分配的固定的唯一识别号 [RemoteFile.id].
 *
 * 通过 [listFiles] 获取到的 [RemoteFile] 都拥有非 `null` 的 [id].
 * 服务器可以通过 [id] 准确定位重名文件中的某一个.
 * 对这样的文件进行 [upload] 时将会覆盖目标文件 (如果存在), 进行 [delete] 时也只会准确操作目标文件.
 *
 * 只要文件内容无变化, 文件的 [id] 就不会变更. 可以保存 [RemoteFile.id] 并在以后通过 [RemoteFile.resolveById] 准确获取一个目标文件.
 *
 * @suppress 使用 [RemoteFile] 是稳定的, 但不应该自行实现这个接口.
 * @see FileSupported
 * @since 2.5
 */
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
     * 标准的绝对路径, 起始字符为 '/'. 如 `/foo/bar.txt`.
     *
     * 根目录路径为 [ROOT_PATH]
     */
    public val path: String

    /**
     * 获取父目录, 当 [RemoteFile] 表示根目录时返回 `null`
     */
    public val parent: RemoteFile?

    /**
     * 此文件所属的群或好友
     */
    public val contact: FileSupported

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
         * 文件长度 (大小) bytes, 目录的 [length] 为 0.
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
         * 上次修改时间. 时间戳秒.
         */
        public val lastModifyTime: Long,
        public val sha1: ByteArray,
        public val md5: ByteArray,
    ) {
        /**
         * 根据 [FileInfo.id] 或 [FileInfo.path] 获取到对应的 [RemoteFile].
         */
        public suspend fun resolveToFile(contact: FileSupported): RemoteFile =
            contact.filesRoot.resolveById(id) ?: contact.filesRoot.resolve(path)
    }

    /**
     * 获取这个文件或目录**此时**的详细信息. 当文件或目录不存在时返回 `null`.
     */
    public suspend fun getInfo(): FileInfo?

    /**
     * 当文件或目录存在时返回 `true`.
     */
    public suspend fun exists(): Boolean

    /**
     * @return [path]
     */
    public override fun toString(): String

    ///////////////////////////////////////////////////////////////////////////
    // resolve
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取该目录的子文件. 不会检查 [RemoteFile] 是否表示一个目录.
     *
     * @param relative  相对路径. 当初始字符为 '/' 时将作为绝对路径解析
     * @see File.resolve stdlib 内的类似函数
     */
    public fun resolve(relative: String): RemoteFile

    /**
     * 获取该目录的子文件. 不会检查 [RemoteFile] 是否表示一个目录. 返回的 [RemoteFile.id] 将会与 `relative.id` 相同.
     *
     * @param relative 相对路径. 当 [RemoteFile.path] 初始字符为 '/' 时将作为绝对路径解析
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

    ///////////////////////////////////////////////////////////////////////////
    // operations
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 删除这个文件或目录. 若目录非空, 则会删除目录中的所有文件. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时返回 `false`.
     */
    public suspend fun delete(): Boolean

    /**
     * 重命名这个文件或目录, 将会更改 [RemoteFile.name] 属性值.
     * 操作非 Bot 自己上传的文件时需要管理员权限.
     *
     * [renameTo] 只会操作远程文件, 而不会修改当前 [RemoteFile.path].
     */
    public suspend fun renameTo(name: String): Boolean

    /**
     * 将这个目录或文件移动到 [target] 位置. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时返回 `false`.
     *
     * [moveTo] 只会操作远程文件, 而不会修改当前 [RemoteFile.path].
     *
     * **注意**: 与 [java.io.File] 类似, 这是将当前 [RemoteFile] 移动到作为 [target], 而不是移动成为 [target] 的子文件或目录. 例如:
     * ```
     * val root = group.filesRoot
     * root.resolve("test.txt").moveTo(root) // 错误! 这是在将该文件的路径 "test.txt" 修改为 “/” , 而不是修改为 "/test.txt"
     * root.resolve("test.txt").moveTo(root.resolve("/")) // 错误! 与上一行相同.

     * root.resolve("/test.txt").moveTo(root.resolve("/test2.txt")) // 正确. 将该文件的路径 "/test.txt" 修改为 “/test2.txt”，相当于重命名文件
     * ```
     *
     * @param target 目标文件位置.
     */
    public suspend fun moveTo(target: RemoteFile): Boolean

    /**
     * 将这个目录或文件移动到另一个位置. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时返回 `false`.
     *
     * [moveTo] 只会操作远程文件, 而不会修改当前 [RemoteFile.path].
     *
     * **已弃用:** 当 [path] 是绝对路径时, 这个函数运行正常;
     * 当它是相对路径时, 将会尝试把当前文件移动到 [RemoteFile.path] 下的子路径 [path], 因此总是失败.
     *
     * 使用参数为 [RemoteFile] 的 [moveTo] 代替.
     *
     * @suppress 在 2.6 弃用. 请使用 [moveTo]
     */
    @Deprecated(
        "Use moveTo(RemoteFile) instead.",
        replaceWith = ReplaceWith("this.moveTo(this.resolveSibling(path))"),
        level = DeprecationLevel.WARNING
    )
    public suspend fun moveTo(path: String): Boolean {
        // Impl notes:
        // if `path` is absolute, this works as intended.
        // if not, `resolve(path)` will be a child path from this dir and fails always.
        return moveTo(resolve(path))
    }

    /**
     * 创建目录. 目录已经存在或无管理员权限时返回 `false`.
     *
     * 创建后 [isDirectory] 也不一定会返回 `true`.
     * 当 [id] 未指定时, [RemoteFile] 总是表示一个路径而无法确定目标是文件还是目录, [isFile] 或 [isDirectory] 结果取决于服务器.
     */
    public suspend fun mkdir(): Boolean

    /**
     * 获取该目录下所有文件, 返回的 [RemoteFile] 都拥有 [RemoteFile.id] 用于区分重名文件或目录. 当 [RemoteFile] 表示一个文件时返回 [emptyFlow].
     *
     * 返回的 [Flow] 是*冷*的, 只会在被需要的时候向服务器查询.
     */
    public suspend fun listFiles(): Flow<RemoteFile>

    /**
     * 获取该目录下所有文件, 返回的 [RemoteFile] 都拥有 [RemoteFile.id] 用于区分重名文件或目录. 当 [RemoteFile] 表示一个文件时返回空迭代器.
     * @param lazy 为 `true` 时惰性获取, 为 `false` 时立即获取全部文件列表.
     */
    @JavaFriendlyAPI
    public suspend fun listFilesIterator(lazy: Boolean): Iterator<RemoteFile>

    /**
     * 获取该目录下所有文件, 返回的 [RemoteFile] 都拥有 [RemoteFile.id] 用于区分重名文件或目录. 当 [RemoteFile] 表示一个文件时返回 [emptyList].
     */
    public suspend fun listFilesCollection(): List<RemoteFile> = listFiles().toList()

    /**
     * 得到相应文件消息, 可以发送. 当 [RemoteFile] 表示一个目录或文件不存在时返回 `null`.
     */
    public suspend fun toMessage(): FileMessage?

    ///////////////////////////////////////////////////////////////////////////
    // upload & download
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 上传进度回调, 可供前端使用, 以提供进度显示.
     * @see asProgressionCallback
     */
    public interface ProgressionCallback {
        /**
         * 当上传开始时调用
         */
        public fun onBegin(file: RemoteFile, resource: ExternalResource) {}

        /**
         * 每当有进度更新时调用. 此方法可能会同时被多个线程调用.
         *
         * 提示: 可通过 [ExternalResource.size] 获取文件总大小.
         */
        public fun onProgression(file: RemoteFile, resource: ExternalResource, downloadedSize: Long) {}

        /**
         * 当上传成功时调用
         */
        public fun onSuccess(file: RemoteFile, resource: ExternalResource) {}

        /**
         * 当上传以异常失败时调用
         */
        public fun onFailure(file: RemoteFile, resource: ExternalResource, exception: Throwable) {}

        public companion object {
            /**
             * 将一个 [SendChannel] 作为 [ProgressionCallback] 使用.
             *
             * 每当有进度更新, 已下载的字节数都会被[发送][SendChannel.offer]到 [SendChannel] 中.
             * 进度的发送会通过 [offer][SendChannel.offer], 而不是通过 [send][SendChannel.send]. 意味着 [SendChannel] 通常要实现缓存.
             *
             * 若 [closeOnFinish] 为 `true`, 当下载完成 (无论是失败还是成功) 时会 [关闭][SendChannel.close] [SendChannel].
             *
             * 使用示例:
             * ```
             * val progress = Channel<Long>(Channel.BUFFERED)
             *
             * launch {
             *   // 每 3 秒发送一次上传进度百分比
             *   progress.receiveAsFlow().sample(3.seconds).collect { bytes ->
             *     group.sendMessage("File upload: ${(bytes.toDouble() / resource.size * 100).toInt() / 100}%.") // 保留 2 位小数
             *   }
             * }
             *
             * group.filesRoot.resolve("/foo.txt").upload(resource, progress.asProgressionCallback(true))
             * group.sendMessage("File uploaded successfully.")
             * ```
             *
             * 直接使用 [ProgressionCallback] 也可以实现示例这样的功能, [asProgressionCallback] 是为了简化操作.
             */
            @JvmStatic
            public fun SendChannel<Long>.asProgressionCallback(closeOnFinish: Boolean = true): ProgressionCallback {
                return object : ProgressionCallback {
                    override fun onProgression(file: RemoteFile, resource: ExternalResource, downloadedSize: Long) {
                        trySend(downloadedSize)
                    }

                    override fun onSuccess(file: RemoteFile, resource: ExternalResource) {
                        if (closeOnFinish) this@asProgressionCallback.close()
                    }

                    override fun onFailure(file: RemoteFile, resource: ExternalResource, exception: Throwable) {
                        if (closeOnFinish) this@asProgressionCallback.close(exception)
                    }
                }
            }
        }
    }

    /**
     * 上传文件到 [RemoteFile] 表示的路径, 上传过程中调用 [callback] 传递进度.
     *
     * 上传后不会发送文件消息, 即官方客户端只能在 "群文件" 中查看文件.
     * 可通过 [toMessage] 获取到文件消息并通过 [Group.sendMessage] 发送, 或使用 [uploadAndSend].
     *
     * ## 已弃用
     *
     * 使用 [sendFile] 代替. 本函数会上传文件但不会发送文件消息.
     * 不发送文件消息就导致其他操作都几乎不能完成, 而且经反馈, 用户通常会忘记后续的 [RemoteFile.toMessage] 操作.
     * 本函数造成了很大的不必要的迷惑, 故以既上传又发送消息的, 与官方客户端行为相同的 [sendFile] 代替.
     *
     * 相关问题: [#1250: 群文件在上传后 toRemoteFile 返回 null](https://github.com/mamoe/mirai/issues/1250)
     *
     *
     * **注意**: [resource] 仅表示资源数据, 而不带有文件名属性.
     * 与 [java.io.File] 类似, [upload] 是将 [resource] 上传成为 [this][RemoteFile], 而不是上传成为 [this][RemoteFile] 的子文件. 示例:
     * ```
     * group.filesRoot.upload(resource) // 错误! 这是在把资源上传成为根目录.
     * group.filesRoot.resolve("/").upload(resource) // 错误! 与上一句相同, 这是在把资源上传成为根目录.
     *
     * val root = group.filesRoot
     * root.resolve("test.txt").upload(resource) // 正确. 把资源上传成为根目录下的 "test.txt".
     * root.resolve("/test.txt").upload(resource) // 正确. 与上一句相同, 把资源上传成为根目录下的 "test.txt".
     * ```
     *
     * @param resource 需要上传的文件资源. 无论上传是否成功, 本函数都不会关闭 [resource].
     * @param callback 进度回调
     * @throws IllegalStateException 该文件上传失败或权限不足时抛出
     */
    @Deprecated(
        "Use uploadAndSend instead.", ReplaceWith("this.uploadAndSend(resource, callback)"), DeprecationLevel.WARNING
    ) // deprecated since 2.7-M1
    public suspend fun upload(
        resource: ExternalResource,
        callback: ProgressionCallback? = null
    ): FileMessage

    /**
     * 上传文件到 [RemoteFile.path] 表示的路径.
     * ## 已弃用
     * 阅读 [upload] 获取更多信息
     * @see upload
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use uploadAndSend instead.", ReplaceWith("this.uploadAndSend(resource)"), DeprecationLevel.WARNING
    )  // deprecated since 2.7-M1
    public suspend fun upload(resource: ExternalResource): FileMessage = upload(resource, null)

    /**
     * 上传文件.
     * ## 已弃用
     * 阅读 [upload] 获取更多信息
     * @see upload
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use uploadAndSend instead.", ReplaceWith("this.uploadAndSend(file, callback)"), DeprecationLevel.WARNING
    ) // deprecated since 2.7-M1
    public suspend fun upload(
        file: File,
        callback: ProgressionCallback? = null
    ): FileMessage = file.toExternalResource().use { upload(it, callback) }

    /**
     * 上传文件.
     * ## 已弃用
     * 阅读 [upload] 获取更多信息
     * @see upload
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use sendFile instead.", ReplaceWith("this.uploadAndSend(file)"), DeprecationLevel.WARNING
    ) // deprecated since 2.7-M1
    public suspend fun upload(file: File): FileMessage = file.toExternalResource().use { upload(it) }

    /**
     * 上传文件并发送文件消息.
     *
     * 若 [RemoteFile.id] 存在且旧文件存在, 将会覆盖旧文件.
     * 即使用 [resolve] 或 [resolveSibling] 获取到的 [RemoteFile] 的 [upload] 总是上传一个新文件,
     * 而使用 [resolveById] 或 [listFiles] 获取到的总是覆盖旧文件, 当旧文件已在远程删除时上传一个新文件.
     *
     * @param resource 需要上传的文件资源. 无论上传是否成功, 本函数都不会关闭 [resource].
     * @see upload
     */
    @MiraiExperimentalApi
    public suspend fun uploadAndSend(resource: ExternalResource): MessageReceipt<Contact>

    /**
     * 上传文件并发送文件消息.
     * @see uploadAndSend
     */
    @MiraiExperimentalApi
    public suspend fun uploadAndSend(file: File): MessageReceipt<Contact> =
        file.toExternalResource().use { uploadAndSend(it) }

    /**
     * 获取文件下载链接, 当文件不存在或 [RemoteFile] 表示一个目录时返回 `null`
     */
    public suspend fun getDownloadInfo(): DownloadInfo?

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

    public companion object {
        /**
         * 根目录路径
         * @see RemoteFile.path
         */
        public const val ROOT_PATH: String = "/"

        /**
         * 上传文件并获取文件消息, 但不发送.
         *
         * ## 已弃用
         * 在 [upload] 获取更多信息
         *
         * @param path 远程路径. 起始字符为 '/'. 如 '/foo/bar.txt'
         * @param resource 需要上传的文件资源. 无论上传是否成功, 本函数都不会关闭 [resource].
         * @see RemoteFile.upload
         */
        @JvmStatic
        @JvmOverloads
        @Deprecated(
            "Use sendFile instead.",
            ReplaceWith(
                "this.sendFile(path, resource, callback)",
                "net.mamoe.mirai.utils.RemoteFile.Companion.sendFile"
            ),
            level = DeprecationLevel.WARNING
        ) // deprecated since 2.7-M1
        public suspend fun FileSupported.uploadFile(
            path: String,
            resource: ExternalResource,
            callback: ProgressionCallback? = null
        ): FileMessage = @Suppress("DEPRECATION") this.filesRoot.resolve(path).upload(resource, callback)

        /**
         * 上传文件并获取文件消息, 但不发送.
         * ## 已弃用
         * 阅读 [uploadFile] 获取更多信息.
         *
         * @param path 远程路径. 起始字符为 '/'. 如 '/foo/bar.txt'
         * @see RemoteFile.upload
         */
        @JvmStatic
        @JvmOverloads
        @Deprecated(
            "Use sendFile instead.",
            ReplaceWith(
                "this.sendFile(path, file, callback)",
                "net.mamoe.mirai.utils.RemoteFile.Companion.sendFile"
            ),
            level = DeprecationLevel.WARNING
        ) // deprecated since 2.7-M1
        public suspend fun FileSupported.uploadFile(
            path: String,
            file: File,
            callback: ProgressionCallback? = null
        ): FileMessage = @Suppress("DEPRECATION") this.filesRoot.resolve(path).upload(file, callback)

        /**
         * 上传文件并发送文件消息到相关 [FileSupported].
         * @param resource 需要上传的文件资源. 无论上传是否成功, 本函数都不会关闭 [resource].
         * @see RemoteFile.uploadAndSend
         */
        @JvmStatic
        @JvmOverloads
        public suspend fun <C : FileSupported> C.sendFile(
            path: String,
            resource: ExternalResource,
            callback: ProgressionCallback? = null
        ): MessageReceipt<C> =
            @Suppress("DEPRECATION")
            this.filesRoot.resolve(path).upload(resource, callback).sendTo(this)

        /**
         * 上传文件并发送文件消息到相关 [FileSupported].
         * @see RemoteFile.uploadAndSend
         */
        @JvmStatic
        @JvmOverloads
        public suspend fun <C : FileSupported> C.sendFile(
            path: String,
            file: File,
            callback: ProgressionCallback? = null
        ): MessageReceipt<C> =
            @Suppress("DEPRECATION")
            this.filesRoot.resolve(path).upload(file, callback).sendTo(this)
    }
}
