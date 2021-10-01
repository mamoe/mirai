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
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.internal.contact.ContactAware
import net.mamoe.mirai.internal.utils.FileSystem
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ProgressionCallback

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
    private val fs get() = FileSystem

    override suspend fun uploadNewFile(
        absolutePath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): AbsoluteFile {
        val normalized = fs.normalize(absolutePath)
        val folder = root.createFolder(normalized.substringBeforeLast("/"))
        val filename = normalized.substringAfterLast('/')
        return folder.uploadNewFile(filename, content, callback)
    }


}