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
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.JavaFriendlyAPI
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
    @JavaFriendlyAPI
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
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        folderId: String,
        filename: String,
        content: ExternalResource,
        keepExisting: Boolean = true,
    ): AbsoluteFile

    /**
     * 上传一个文件到指定精确路径. 返回 [fileId].
     *
     * @param fileId 绝对文件标识.
     * @param content 文件内容
     * @param keepExisting 为 `false` 时删除远程目录内所有同名文件 (**谨慎操作**, 有可能意想不到地删除多个同名文件), 为 `true` 时则不作操作 (将有可能产生同名文件).
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        fileId: AbsoluteFile,
        content: ExternalResource,
        keepExisting: Boolean = true,
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
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFileSlow(
        absolutePath: String,
        content: ExternalResource,
        keepExisting: Boolean = true,
    ): AbsoluteFile
}

