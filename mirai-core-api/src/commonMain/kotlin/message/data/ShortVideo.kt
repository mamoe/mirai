/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.safeCast

public interface ShortVideo : MessageContent, ConstrainSingle {
    /**
     * 文件 ID.
     */
    public val fileId: String

    /**
     * 文件 MD5. 16 bytes.
     */
    public val fileMd5: ByteArray

    /*
     * 文件大小
     */
    public val fileSize: Long

    /**
     * 文件类型
     */
    public val fileFormat: String


    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitShortVideo(this, data)
    }

    override val key: MessageKey<*>
        get() = Key


    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, ShortVideo>(MessageContent, { it.safeCast() })
}

@NotStableForInheritance
public interface OnlineShortVideo : ShortVideo {
    /*
     * 文件名
     */
    public val fileName: String

    /**
     * 下载链接
     */
    public val urlForDownload: String

    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, OnlineShortVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "OnlineShortVideo"
    }
}

@NotStableForInheritance
public interface OfflineShortVideo : ShortVideo {

    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, OfflineShortVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "OfflineShortVideo"
    }
}