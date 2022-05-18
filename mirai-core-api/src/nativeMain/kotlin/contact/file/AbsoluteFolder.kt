/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.file

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.ProgressionCallback

/**
 * 绝对目录标识. 精确表示一个远程目录. 不会受同名文件或目录的影响.
 *
 * @since 2.8
 * @see RemoteFiles
 * @see AbsoluteFile
 * @see AbsoluteFileFolder
 */
@Suppress("SEALED_INHERITOR_IN_DIFFERENT_MODULE")
@NotStableForInheritance
public actual interface AbsoluteFolder : AbsoluteFileFolder {
    /**
     * 当前快照中文件数量, 当有文件更新时(上传/删除文件) 该属性不会更新.
     *
     * 只可能通过 [refresh] 手动刷新
     *
     * 特别的, 若该目录表示根目录, [contentsCount] 返回 `0`. (无法快速获取)
     */
    public actual val contentsCount: Int

    /**
     * 当该目录为空时返回 `true`.
     */
    public actual fun isEmpty(): Boolean = contentsCount == 0

    /**
     * 返回更新了文件或目录信息 ([lastModifiedTime] 等) 的, 指向相同文件的 [AbsoluteFileFolder].
     * 不会更新当前 [AbsoluteFileFolder] 对象.
     *
     * 当远程文件或目录不存在时返回 `null`.
     *
     * 该函数会遍历上级目录的所有文件并匹配当前文件, 因此可能会非常慢, 请不要频繁使用.
     */
    actual override suspend fun refreshed(): AbsoluteFolder?

    ///////////////////////////////////////////////////////////////////////////
    // list children
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取该目录下所有子目录列表.
     */
    public actual suspend fun folders(): Flow<AbsoluteFolder>

    /**
     * 获取该目录下所有文件列表.
     */
    public actual suspend fun files(): Flow<AbsoluteFile>


    /**
     * 获取该目录下所有文件和子目录列表.
     */
    public actual suspend fun children(): Flow<AbsoluteFileFolder>

    ///////////////////////////////////////////////////////////////////////////
    // resolve and upload
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 创建一个名称为 [name] 的子目录. 返回成功创建的或已有的子目录. 当目标目录已经存在时则直接返回该目录.
     *
     * @throws IllegalArgumentException 当 [name] 为空或包含非法字符 (`:*?"<>|`) 时抛出
     * @throws PermissionDeniedException 当权限不足时抛出
     */
    public actual suspend fun createFolder(name: String): AbsoluteFolder

    /**
     * 获取一个已存在的名称为 [name] 的子目录. 当该名称的子目录不存在时返回 `null`.
     *
     * @throws IllegalArgumentException 当 [name] 为空或包含非法字符 (`:*?"<>|`) 时抛出
     */
    public actual suspend fun resolveFolder(name: String): AbsoluteFolder?

    /**
     * 获取一个已存在的 [AbsoluteFileFolder.id] 为 [id] 的子目录. 当该名称的子目录不存在时返回 `null`.
     *
     * @throws IllegalArgumentException 当 [id] 为空或无效时抛出
     *
     * @since 2.9.0
     */
    public actual suspend fun resolveFolderById(id: String): AbsoluteFolder?

    /**
     * 精确获取 [AbsoluteFile.id] 为 [id] 的文件. 在目标文件不存在时返回 `null`. 当 [deep] 为 `true` 时还会深入子目录查找.
     */
    public actual suspend fun resolveFileById(
        id: String,
        deep: Boolean
    ): AbsoluteFile?

    /**
     * 根据路径获取指向的所有路径为 [path] 的文件列表. 同时支持相对路径和绝对路径. 支持获取子目录内的文件.
     */
    public actual suspend fun resolveFiles(
        path: String
    ): Flow<AbsoluteFile>

    /**
     * 根据路径获取指向的所有路径为 [path] 的文件和目录列表. 同时支持相对路径和绝对路径. 支持获取子目录内的文件和目录.
     */
    public actual suspend fun resolveAll(
        path: String
    ): Flow<AbsoluteFileFolder>

    /**
     * 上传一个文件到该目录, 返回上传成功的文件标识.
     *
     * 会在必要时尝试创建远程目录.
     *
     * ### [filepath]
     *
     * - 可以是 `foo.txt` 表示该目录下的文件 "foo.txt"
     * - 也可以是 `sub/foo.txt` 表示该目录的子目录 "sub" 下的文件 "foo.txt".
     * - 或是绝对路径 `/sub/foo.txt` 表示根目录的 "sub" 目录下的文件 "foo.txt"
     *
     * @param filepath 目标文件名
     * @param content 文件内容
     * @param callback 下载进度回调, 传递的 `progression` 是已下载字节数.
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    public actual suspend fun uploadNewFile(
        filepath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?,
    ): AbsoluteFile

    public actual companion object {
        /**
         * 根目录 folder ID.
         * @see id
         */
        public actual const val ROOT_FOLDER_ID: String = "/"
    }
}