/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x6d6
import net.mamoe.mirai.internal.network.protocol.packet.chat.CommonOidbResponse
import net.mamoe.mirai.internal.utils.FileSystem

/**
 * Abstract protocol bridge for file management.
 */
internal interface FileProtocol {
    val fs: FileSystem get() = FileSystem

    fun renameFile(
        client: QQAndroidClient,
        groupCode: Long,
        busId: Int,
        fileId: String,
        parentFolderId: String,
        newName: String,
    ): CommonOidbResponse<Oidb0x6d6.RenameFileRspBody>
}