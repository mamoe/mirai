/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.internal.contact.ContactAware
import net.mamoe.mirai.internal.utils.FileSystem

internal class RemoteFilesImpl(
    override val contact: FileSupported,
    override val root: AbsoluteFolder = AbsoluteFolderImpl(
        contact,
        null,
        AbsoluteFolder.ROOT_FOLDER_ID,
        "/",
        0,
        0,
        0,
        0
    ),
) : RemoteFiles, ContactAware {
    companion object {
        suspend fun AbsoluteFolder.findFileByPath(path: String): Pair<AbsoluteFolder, String> {
            if (path.isBlank()) throw IllegalArgumentException("absolutePath cannot be blank.")
            val normalized = FileSystem.normalize(path)
//            if (!normalized.contains('/')) {
//                throw IllegalArgumentException("Invalid absolutePath: '$path'. If you wanted to upload file to root directory, please add a leading '/'.")
//            }
            val folder = when (normalized.count { it == '/' }) {
                0, 1 -> this
                else -> this.createFolder(normalized.substringBeforeLast("/"))
            }

            val filename = normalized.substringAfterLast('/')
            return folder to filename
        }
    }

}