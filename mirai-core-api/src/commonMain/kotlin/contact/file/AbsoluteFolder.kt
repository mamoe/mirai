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
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.NotStableForInheritance
import java.util.stream.Stream

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
    @JavaFriendlyAPI
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
    @JavaFriendlyAPI
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
    @JavaFriendlyAPI
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
     * 根据路径获取指向的所有名称为 [name] 的文件列表.
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [resolveFiles], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [resolveFiles].
     */
    public suspend fun resolveFilesStream(
        name: String
    ): Stream<AbsoluteFile>

    /**
     * 根据路径获取指向的所有名称为 [name] 的文件和目录列表.
     */
    public suspend fun resolveAll(
        name: String
    ): Flow<AbsoluteFileFolder>

    /**
     * 根据路径获取指向的所有名称为 [name] 的文件和目录列表.
     *
     * 实现细节: 为了适合 Java 调用, 实现类似为阻塞式的 [resolveAll], 因此不建议在 Kotlin 使用. 在 Kotlin 请使用 [resolveAll].
     */
    @JavaFriendlyAPI
    public suspend fun resolveAllStream(
        name: String
    ): Stream<AbsoluteFileFolder>

    /**
     * 上传一个文件到该目录, 返回上传成功的文件标识.
     *
     * @param filename 目标文件名
     * @param content 文件内容
     * @param keepExisting 为 `false` 时删除远程目录内所有同名文件 (**谨慎操作**, 有可能意想不到地删除多个同名文件), 为 `true` 时则不作操作 (将有可能产生同名文件).
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        filename: String,
        content: ExternalResource,
        keepExisting: Boolean = true,
    ): AbsoluteFile

    public companion object {
        /**
         * 根目录 folder ID.
         * @see id
         */
        public const val ROOT_FOLDER_ID: String = "/"
    }
}