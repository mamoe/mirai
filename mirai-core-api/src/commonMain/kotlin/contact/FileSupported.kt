/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.RemoteFile

/**
 * 支持文件操作的 [Contact]. 目前仅 [Group]
 * @since 2.5
 */
@MiraiExperimentalApi
public interface FileSupported : Contact {
    /**
     * 文件根目录. 可通过 [RemoteFile.listFiles] 获取目录下文件列表.
     *
     * @since 2.5
     */
    @MiraiExperimentalApi
    public val filesRoot: RemoteFile
}