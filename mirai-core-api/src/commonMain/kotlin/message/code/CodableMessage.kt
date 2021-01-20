/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.code

import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi


/**
 * 可以使用 mirai 码表示的 [Message] 类型.
 *
 * 从字符串解析 mirai 码：[MiraiCode.deserializeMiraiCode]
 *
 * @see At
 * @see AtAll
 * @see VipFace
 * @see Face
 * @see Image
 * @see FlashImage
 * @see PokeMessage
 *
 * @see MiraiCode
 */
public interface CodableMessage : Message {
    /**
     * 转换为 mirai 码.
     */
    public fun serializeToMiraiCode(): String = buildString { appendMiraiCodeTo(this) }

    // Using StringBuilder faster than direct plus objects
    @MiraiExperimentalApi
    public fun appendMiraiCodeTo(builder: StringBuilder)
}