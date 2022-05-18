/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.AbsoluteFileFolder
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x6d8
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.toResult
import net.mamoe.mirai.internal.utils.FileSystem
import net.mamoe.mirai.utils.JavaFriendlyAPI
import java.util.stream.Stream
import kotlin.streams.asStream

internal actual class AbsoluteFolderImpl actual constructor(
    contact: FileSupported, parent: AbsoluteFolder?, id: String, name: String,
    uploadTime: Long, uploaderId: Long, lastModifiedTime: Long,
    contentsCount: Int,
) : CommonAbsoluteFolderImpl(contact, parent, id, name, uploadTime, uploaderId, lastModifiedTime, contentsCount) {

    @JavaFriendlyAPI
    override suspend fun foldersStream(): Stream<AbsoluteFolder> {
        return getItemsSequence().filter { it.folderInfo != null }.map { it.resolve() as AbsoluteFolder }.asStream()
    }

    @JavaFriendlyAPI
    override suspend fun filesStream(): Stream<AbsoluteFile> {
        return getItemsSequence().filter { it.fileInfo != null }.map { it.resolve() as AbsoluteFile }.asStream()
    }

    @JavaFriendlyAPI
    override suspend fun childrenStream(): Stream<AbsoluteFileFolder> {
        return getItemsSequence().mapNotNull { it.resolve() }.asStream()
    }

    @JavaFriendlyAPI
    private suspend fun getItemsSequence(): Sequence<Oidb0x6d8.GetFileListRspBody.Item> {
        return sequence {
            var index = 0
            while (true) {
                val list = runBlocking {
                    bot.network.sendAndExpect(
                        FileManagement.GetFileList(
                            client,
                            groupCode = contact.id,
                            folderId = id,
                            startIndex = index
                        )
                    )
                }.toResult("AbsoluteFolderImpl.getFilesFlow").getOrThrow()
                index += list.itemList.size

                if (list.int32RetCode != 0) return@sequence
                if (list.itemList.isEmpty()) return@sequence

                yieldAll(list.itemList)
            }
        }
    }

    @OptIn(JavaFriendlyAPI::class)
    override suspend fun resolveFilesStream(path: String): Stream<AbsoluteFile> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (!FileSystem.isLegal(path)) return Stream.empty()

        if (path[0] == '/') {
            return root.resolveFilesStream(path.substring(1))
        }

        if (!path.contains('/')) {
            return getItemsSequence()
                .filter { it.fileInfo?.fileName == path }
                .map { it.resolve() as AbsoluteFile }
                .asStream()
        }

        return resolveFolder(path.substringBefore('/'))?.resolveFilesStream(path.substringAfter('/')) ?: Stream.empty()
    }

    @JavaFriendlyAPI
    override suspend fun resolveAllStream(path: String): Stream<AbsoluteFileFolder> {
        if (path.isBlank()) throw IllegalArgumentException("path cannot be blank.")
        if (!FileSystem.isLegal(path)) return Stream.empty()
        if (path[0] == '/') {
            return root.resolveAllStream(path.substring(1))
        }
        if (!path.contains('/')) {
            return getItemsSequence().mapNotNull { it.resolve() }.asStream()
        }

        return resolveFolder(path.substringBefore('/'))?.resolveAllStream(path.substringAfter('/')) ?: Stream.empty()
    }
}
