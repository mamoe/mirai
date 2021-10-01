/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge
@file:Suppress("OVERLOADS_INTERFACE")

package net.mamoe.mirai.contact.file

import kotlinx.coroutines.flow.Flow
import kotlinx.io.errors.IOException
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import java.util.stream.Stream

/**
 * 表示远程文件列表 (管理器).
 *
 * [RemoteFiles] 包含一些协议接口,
 *
 * TODO docs
 *
 * @since 2.8
 *
 * @see FileSupported
 */
@NotStableForInheritance
public interface RemoteFiles {
    /**
     * 获取表示根目录的 [AbsoluteFolder]
     */
    public val root: AbsoluteFolder

    /**
     * 该对象所属 [FileSupported]
     */
    public val contact: FileSupported


    /**
     * 获取包含所有文件和目录列表的 [Flow].
     *
     * **注意**: 当文件数量较多时, 获取整个列表可能消耗非常长时间 (数分钟). 请考虑减少查询数量和次数.
     *
     * @param deep 为 `true` 时深入二级目录.
     */
    @JvmOverloads
    public suspend fun asFlow(deep: Boolean = true): Flow<AbsoluteFileFolder>

    /**
     * 获取包含所有文件和目录列表的 [Flow].
     *
     * **注意**: 当文件数量较多时, 获取整个列表可能消耗非常长时间 (数分钟). 请考虑减少查询数量和次数.
     *
     * @param deep 为 `true` 时深入二级目录.
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [asFlow], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [asFlow].
     */
    @JvmOverloads
    public fun asStream(deep: Boolean = true): Stream<AbsoluteFileFolder>


    /**
     * 上传一个文件到指定目录, 返回上传成功的文件标识.
     *
     * 也可以使用 [AbsoluteFolder.uploadNewFile].
     *
     * @param folderId 目录
     * @param filename 目标文件名
     * @param content 文件内容
     * @param keepExisting 为 `false` 时删除远程目录内所有同名文件 (**谨慎操作**, 有可能意想不到地删除多个同名文件), 为 `true` 时则不作操作 (将有可能产生同名文件).
     * @param quietly 为 `false` 时将会在群内发送一条 "新文件" 消息 (标准行为). 为 `true` 时则只上传到群文件而不告知群成员.
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        folderId: String,
        filename: String,
        content: ExternalResource,
        keepExisting: Boolean = true,
        quietly: Boolean = false,
    ): AbsoluteFile

    /**
     * 上传一个文件到指定精确路径. 返回 [fileId].
     *
     * @param fileId 绝对文件标识.
     * @param content 文件内容
     * @param keepExisting 为 `false` 时删除远程目录内所有同名文件 (**谨慎操作**, 有可能意想不到地删除多个同名文件), 为 `true` 时则不作操作 (将有可能产生同名文件).
     * @param quietly 为 `false` 时将会在群内发送一条 "新文件" 消息 (标准行为). 为 `true` 时则只上传到群文件而不告知群成员.
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        fileId: AbsoluteFile,
        content: ExternalResource,
        keepExisting: Boolean = true,
        quietly: Boolean = false,
    ): AbsoluteFile

    /**
     * 上传一个文件到指定精确路径. 返回指代该远程文件的 [AbsoluteFile].
     *
     * 注意使用 [absolutePath] 将会慢于使用 [AbsoluteFile] 等精确参数. 请优先考虑该函数的相关重载, 尤其是在操作大量文件时.
     *
     * **频繁使用本重载可能导致严重性能下降**, 因为每次操作时都要遍历所有目录直到找到目标路径.
     * 仅建议在测试时等性能不敏感环境使用.
     *
     * @param absolutePath 绝对文件路径, **包含目标文件名**. 如 `/foo/bar.txt`.
     * @param content 文件内容
     * @param keepExisting 为 `false` 时删除远程目录内所有同名文件 (**谨慎操作**, 有可能意想不到地删除多个同名文件), 为 `true` 时则不作操作 (将有可能产生同名文件).
     * @param quietly 为 `false` 时将会在群内发送一条 "新文件" 消息 (标准行为). 为 `true` 时则只上传到群文件而不告知群成员.
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFileSlow(
        absolutePath: String,
        content: ExternalResource,
        keepExisting: Boolean = true,
        quietly: Boolean = false,
    ): AbsoluteFile
}

/**
 * 绝对文件或目录标识. 精确表示一个远程文件. 不会受同名文件或目录的影响.
 *
 * @since 2.8
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
     * 文件或目录的 ID, 即 `fileId` 和 `folderId`. 该属性由服务器维护, 通常唯一且持久.
     */
    public val id: String

    /**
     * 文件名或目录名. 该属性不会因 [renameTo] 等操作而变化.
     *
     * 不能包含 `:*?"<>|/\` 任一字符.
     */
    public val name: String

    /**
     * 绝对路径, 如 `/foo/bar.txt`
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
     * 远程文件或目录的创建时间, UTC+8 时间戳秒.
     */
    public val creationTime: Long

    /**
     * 远程文件或目录的最后修改时间, UTC+8 时间戳秒.
     */
    public val lastModifiedTime: Long


    /**
     * 重命名远程文件或目录, 但不修改 [name]. 成功时返回 `true`, 当远程文件或目录不存在时返回 `false`.
     *
     * 注意该操作有可能产生同名文件或目录 (当服务器已经存在一个名称为 [newName] 的文件或目录时).
     *
     * @throws IOException 当发生网络错误时可能抛出
     * @throws IllegalStateException 当发生已知的协议错误时抛出
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    public suspend fun renameTo(newName: String): Boolean

    /**
     * 移动远程文件或目录成为 [folderId] 的子文件或子目录. 成功时返回 `true`, 当远程文件或目录不存在时返回 `false`.
     *
     * 注意该操作有可能产生同名文件或目录 (当 [folderId] 中已经存在一个名称为 [name] 的文件或目录时).
     *
     * @throws IOException 当发生网络错误时可能抛出
     * @throws IllegalStateException 当发生已知的协议错误时抛出
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    public suspend fun moveTo(folderId: AbsoluteFolder): Boolean

    /**
     * 删除远程文件或目录. 只会根据 [id] 精确地删除一个文件或目录, 不会删除其他同名文件或目录.
     *
     * 若目录非空, 则会删除目录中的所有文件. 操作目录或非 Bot 自己上传的文件时需要管理员权限, 无管理员权限时抛出异常.
     *
     * @throws IOException 当发生网络错误时可能抛出
     * @throws IllegalStateException 当发生已知的协议错误时抛出
     * @throws PermissionDeniedException 当无管理员权限时抛出
     */
    public suspend fun delete(): Boolean

    /**
     * 更新当前 [AbsoluteFileFolder] 对象的文件或目录信息 ([lastModifiedTime] 等).
     *
     * 该函数会遍历上级目录的所有文件并匹配当前文件, 因此可能会非常慢, 请不要频繁使用.
     */
    public suspend fun refresh()

    /**
     * 返回更新了文件或目录信息 ([lastModifiedTime] 等) 的, 指向相同文件的 [AbsoluteFileFolder]. 不会更新当前 [AbsoluteFileFolder] 对象.
     *
     * 该函数会遍历上级目录的所有文件并匹配当前文件, 因此可能会非常慢, 请不要频繁使用.
     */
    public suspend fun refreshed(): AbsoluteFileFolder
}

/**
 * 绝对目录标识. 精确表示一个远程目录. 不会受同名文件或目录的影响.
 *
 * @since 2.8
 */
@NotStableForInheritance
public interface AbsoluteFolder : AbsoluteFileFolder {
    override val isFile: Boolean get() = false
    override val isFolder: Boolean get() = true

    /**
     * 目录内文件数量. 若该目录表示根目录, [contentsCount] 返回 `0`. (无法快速获取)
     */
    public val contentsCount: Long

    /**
     * 当该目录为空时返回 `true`.
     */
    public suspend fun isEmpty(): Boolean


    ///////////////////////////////////////////////////////////////////////////
    // list children
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取该目录下所有子目录列表.
     */
    public suspend fun folders(): Flow<AbsoluteFolder>

    /**
     * 获取该目录下所有子目录列表.
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [folders], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [folders].
     */
    public suspend fun foldersStream(): Stream<AbsoluteFolder>


    /**
     * 获取该目录下所有文件列表.
     */
    public suspend fun files(): Flow<AbsoluteFile>

    /**
     * 获取该目录下所有文件列表.
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [files], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [files].
     */
    public suspend fun filesStream(): Stream<AbsoluteFile>


    /**
     * 获取该目录下所有文件和子目录列表.
     */
    public suspend fun children(): Flow<AbsoluteFileFolder>

    /**
     * 获取该目录下所有文件和子目录列表.
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [children], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [children].
     */
    public suspend fun childrenStream(): Stream<AbsoluteFileFolder>

    ///////////////////////////////////////////////////////////////////////////
    // resolve and upload
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 创建一个名称为 [name] 的子目录. 返回成功创建的或已有的子目录.
     */
    public suspend fun createFolder(mame: String): AbsoluteFolder

    /**
     * 获取一个已存在的名称为 [name] 的子目录. 当该名称的子目录不存在时返回 `null`.
     */
    public suspend fun resolveFolder(name: String): AbsoluteFolder?

    /**
     * 根据路径获取指向的所有名称为 [name] 的文件列表.
     */
    public suspend fun resolveFiles(
        name: String
    ): Flow<AbsoluteFile>

    /**
     * 根据路径获取指向的所有名称为 [name] 的文件和目录列表.
     */
    public suspend fun resolveAll(
        name: String
    ): Flow<AbsoluteFileFolder>

    /**
     * 上传一个文件到该目录, 返回上传成功的文件标识.
     *
     * @param filename 目标文件名
     * @param content 文件内容
     * @param keepExisting 为 `false` 时删除远程目录内所有同名文件 (**谨慎操作**, 有可能意想不到地删除多个同名文件), 为 `true` 时则不作操作 (将有可能产生同名文件).
     * @param quietly 为 `false` 时将会在群内发送一条 "新文件" 消息 (标准行为). 为 `true` 时则只上传到群文件而不告知群成员.
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        filename: String,
        content: ExternalResource,
        keepExisting: Boolean = true,
        quietly: Boolean = false,
    ): AbsoluteFile

    public companion object {
        /**
         * 根目录 folder ID.
         * @see id
         */
        public const val ROOT_FOLDER_ID: String = "/"
    }
}

/**
 * 绝对文件标识. 精确表示一个远程文件. 不会受同名文件或目录的影响.
 *
 * @since 2.8
 */
@NotStableForInheritance
public interface AbsoluteFile : AbsoluteFileFolder {
    override val isFile: Boolean get() = true
    override val isFolder: Boolean get() = false

    /**
     * 文件到期时间, UTC+8 时间戳秒.
     */
    public val expiryTime: Long

    /**
     * 上传者 ID.
     */
    public val uploaderId: Long

    /**
     * 文件大小 (占用空间) bytes.
     */
    public val size: Long

    /**
     * 文件内容 SHA-1.
     */
    public val sha1: ByteArray

    /**
     * 文件内容 MD5.
     */
    public val md5: ByteArray

    /**
     * 覆盖远程文件内容为 [content]. 当远程文件不存在时返回 `false`.
     *
     * @throws IOException 当发生网络错误时可能抛出
     * @throws IllegalStateException 当发生已知的协议错误时抛出
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun overrideWith(content: ExternalResource, condoneMissing: Boolean = true): Boolean

    /**
     * 将在线文件转换为 [ExternalResource]. 注意该函数不会立即下载文件. 若在之后的使用中远程文件被删除, 则无法确定 [ExternalResource.inputStream] 是否还可用 (不可用时会抛出异常).
     */
    public suspend fun asResource(): ExternalResource

    /**
     * 获得下载链接 URL 字符串.
     */
    public suspend fun getUrl(): String

    /**
     * 得到表示远程文件的可以发送的 [FileMessage].
     *
     * 在 [上传文件][RemoteFiles.uploadNewFile] 时若 `quietly` 参数为 `false` 则已经发送了文件消息, 无需手动调用 [toMessage] 并发送.
     */
    public fun toMessage(): FileMessage
}