/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge
@file:Suppress("OVERLOADS_INTERFACE")

package net.mamoe.mirai.contact.file

import io.ktor.utils.io.errors.*
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.NotStableForInheritance
import kotlin.jvm.JvmStatic

/**
 * 绝对文件或目录标识. 精确表示一个远程文件. 不会受同名文件或目录的影响.
 *
 * @since 2.8
 * @see RemoteFiles
 * @see AbsoluteFile
 * @see AbsoluteFolder
 */
@NotStableForInheritance
public sealed interface AbsoluteFileFolder {
    /**
     * 该对象所属 [FileSupported]
     */
    public val contact: FileSupported

    /**
     * 上级 [AbsoluteFileFolder].
     *
     * - 当该 [AbsoluteFileFolder] 表示一个目录中的文件时返回文件所属目录的 [AbsoluteFolder].
     * - 当该 [AbsoluteFileFolder] 表示子目录时返回父目录的 [AbsoluteFolder].
     *
     * 特别地,
     * - 当该 [AbsoluteFileFolder] 表示根目录下的一个文件时返回根目录的 [AbsoluteFolder].
     * - 当该 [AbsoluteFileFolder] 表示根目录时返回 `null` (表示无上级).
     *
     * 也就是说, 若 [AbsoluteFileFolder.parent] 为 `null`, 那么该 [AbsoluteFileFolder] 就表示根目录.
     */
    public val parent: AbsoluteFolder?

    /**
     * 文件或目录的 ID, 即 `fileId` 或 `folderId`. 该属性由服务器维护, 通常唯一且持久.
     */
    public val id: String

    /**
     * 文件名或目录名.
     *
     * 注意, 当远程文件或目录被 (其他人) 改名时, [name] 不会变动.
     * 只有在调用 [renameTo] 和 [refresh] 时才会更新.
     *
     * 不会包含 `:*?"<>|/\` 任一字符.
     */
    public val name: String

    /**
     * 绝对路径, 如 `/foo/bar.txt`.
     *
     * 注意, 当远程文件或目录被 (其他人) 移动到其他位置或其父目录名称改名时, [absolutePath] 不会变动.
     * 只有在调用 [renameTo] 和 [refresh] 等时才会更新.
     */
    public val absolutePath: String

    /**
     * 表示远程文件时返回 `true`.
     */
    public val isFile: Boolean

    /**
     * 表示远程目录时返回 `true`.
     */
    public val isFolder: Boolean

    /**
     * 远程文件或目录的创建时间, 时间戳秒.
     */
    public val uploadTime: Long

    /**
     * 远程文件或目录的最后修改时间戳, 单位秒.
     *
     * 注意, 当远程文件或目录被 (其他人) 改动时, [lastModifiedTime] 不会变动.
     * 只有在调用 [renameTo] 和 [refresh] 等时才会更新.
     */
    public val lastModifiedTime: Long

    /**
     * 上传者 ID.
     */
    public val uploaderId: Long


    /**
     * 查询该远程文件或目录是否还存在于服务器.
     *
     * 只会精确地按 [id] 检查, 而不会考虑同名文件或目录. 当文件或目录存在时返回 `true`.
     *
     * 该操作不会更新 [absolutePath] 等属性.
     */
    public suspend fun exists(): Boolean

    /**
     * 重命名远程文件或目录, **并且**修改当前(`this`) [AbsoluteFileFolder] 的 [name].
     * 成功时返回 `true`, 当远程文件或目录不存在时返回 `false`.
     *
     * 注意该操作有可能产生同名文件或目录 (当服务器已经存在一个名称为 [newName] 的文件或目录时).
     *
     * @throws IOException 当发生网络错误时可能抛出
     * @throws IllegalStateException 当发生已知的协议错误时抛出
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    public suspend fun renameTo(newName: String): Boolean

    /**
     * 删除远程文件或目录. 只会根据 [id] 精确地删除一个文件或目录, 不会删除其他同名文件或目录.
     * 成功时返回 `true`, 当远程文件或目录不存在时返回 `false`.
     *
     * 若目录非空, 则会删除目录中的所有文件. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时抛出异常.
     *
     * @throws IOException 当发生网络错误时可能抛出
     * @throws IllegalStateException 当发生已知的协议错误时抛出
     * @throws PermissionDeniedException 当无管理员权限时抛出
     */
    public suspend fun delete(): Boolean

    /**
     * 更新当前 [AbsoluteFileFolder] 对象的文件或目录信息 ([lastModifiedTime], [absolutePath] 等).
     * 成功时返回 `true`, 当远程文件或目录不存在时返回 `false`.
     */
    public suspend fun refresh(): Boolean

    /**
     * 返回更新了文件或目录信息 ([lastModifiedTime] 等) 的, 指向相同文件的 [AbsoluteFileFolder].
     * 不会更新当前 [AbsoluteFileFolder] 对象.
     *
     * 当远程文件或目录不存在时返回 `null`.
     *
     * 该函数会遍历上级目录的所有文件并匹配当前文件, 因此可能会非常慢, 请不要频繁使用.
     */
    public suspend fun refreshed(): AbsoluteFileFolder?

    public override fun toString(): String

    public companion object {
        /**
         * 返回去掉文件后缀的文件名. 如 `foo.txt` 返回 `foo`.
         *
         * 注意, 当远程文件或目录被 (其他人) 改名时, [nameWithoutExtension] 不会变动.
         * 只有在调用 [renameTo] 和 [refresh] 时才会更新.
         *
         * 不会包含 `:*?"<>|/\` 任一字符.
         *
         * @see File.nameWithoutExtension
         */
        @get:JvmStatic
        public val AbsoluteFileFolder.nameWithoutExtension: String
            get() = name.substringBeforeLast('.')

        /**
         * 返回文件的后缀名. 如 `foo.txt` 返回 `txt`.
         *
         * 注意, 当远程文件或目录被 (其他人) 改名时, [extension] 不会变动.
         * 只有在调用 [renameTo] 和 [refresh] 时才会更新.
         *
         * 不会包含 `:*?"<>|/\` 任一字符.
         *
         * @see java.io.File.extension
         */
        @get:JvmStatic
        public val AbsoluteFileFolder.extension: String
            get() = name.substringAfterLast('.', "")
    }
}
