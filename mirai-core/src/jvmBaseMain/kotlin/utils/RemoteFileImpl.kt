/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.RemoteFile
import java.io.File

internal actual class RemoteFileImpl actual constructor(
    contact: Group,
    path: String
) : CommonRemoteFileImpl(contact, path), RemoteFile {
    actual constructor(contact: Group, parent: String, name: String) : this(contact, FileSystem.normalize(parent, name))

    // compiler bug
    @Deprecated(
        "Use uploadAndSend instead.",
        replaceWith = ReplaceWith("this.uploadAndSend(file, callback)"),
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    override suspend fun upload(file: File, callback: RemoteFile.ProgressionCallback?): FileMessage =
        file.toExternalResource().use { upload(it, callback) }

    //compiler bug
    @Deprecated(
        "Use sendFile instead.",
        replaceWith = ReplaceWith("this.uploadAndSend(file)"),
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    override suspend fun upload(file: File): FileMessage {
        // Dear compiler:
        //
        // Please generate invokeinterface.
        //
        // Yours Sincerely
        // Him188
        return file.toExternalResource().use { upload(it) }
    }

    // compiler bug
    override suspend fun uploadAndSend(file: File): MessageReceipt<Contact> =
        file.toExternalResource().use { uploadAndSend(it) }

    //    override suspend fun writeSession(resource: ExternalResource): FileUploadSession {
    //    }

}