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

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.ProgressionCallback

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
     * 上传一个文件到指定精确路径. 返回指代该远程文件的 [AbsoluteFile].
     *
     * 会在必要时尝试创建远程目录.
     *
     * 也可以使用 [AbsoluteFolder.uploadNewFile].
     *
     * @param absolutePath 绝对文件路径, **包含目标文件名**. 如 `/foo/bar.txt`.
     * @param content 文件内容
     * @param callback 下载进度回调, 传递的 `progression` 是已下载字节数.
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        absolutePath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>? = null,
    ): AbsoluteFile
}

