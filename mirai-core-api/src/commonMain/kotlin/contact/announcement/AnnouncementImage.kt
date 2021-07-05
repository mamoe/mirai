/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.announcement

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 群公告图片. 可通过 [Announcements.uploadImage] 上传获得. 不确定服务器会保存多久.
 * @since 2.7
 */
@SerialName(AnnouncementImage.SERIAL_NAME)
@Serializable
public class AnnouncementImage @MiraiInternalApi public constructor(
    public val height: String,
    public val width: String,
    public val id: String
) {
    // For stability, do not make it `data class`.

    public companion object {
        public const val SERIAL_NAME: String = "AnnouncementImage"
    }
}