/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.image

import net.mamoe.mirai.internal.message.data.OfflineShortVideoImpl
import net.mamoe.mirai.internal.message.data.ShortVideoThumbnail
import net.mamoe.mirai.message.data.InternalShortVideoProtocol
import net.mamoe.mirai.message.data.OfflineShortVideo

internal class InternalShortVideoProtocolImpl : InternalShortVideoProtocol {
    override fun createOfflineShortVideo(
        videoId: String,
        fileMd5: ByteArray,
        fileSize: Long,
        fileFormat: String,
        fileName: String,
        thumbnailMd5: ByteArray,
        thumbnailSize: Long
    ): OfflineShortVideo {
        return OfflineShortVideoImpl(
            videoId,
            fileName,
            fileMd5,
            fileSize,
            fileFormat,
            ShortVideoThumbnail(
                thumbnailMd5,
                thumbnailSize,
                0,
                0
            )
        )
    }
}