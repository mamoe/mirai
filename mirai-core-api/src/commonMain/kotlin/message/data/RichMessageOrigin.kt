/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.IMirai
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast

/**
 * 标识一个长消息.
 *
 *
 * 消息过长后会通过特殊的通道上传和下载, 每条消息都会获得一个 resourceId.
 *
 * 可以通过 resourceId 下载消息 [IMirai.downloadLongMessage].
 * 但不保证 resourceId 一直有效.
 *
 * @since 2.3
 */
@MiraiExperimentalApi
public data class RichMessageOrigin(
    val resourceId: String
) : MessageMetadata, ConstrainSingle {
    override val key: Key get() = Key

    override fun toString(): String = ""

    public companion object Key : AbstractMessageKey<RichMessageOrigin>({ it.safeCast() })
}