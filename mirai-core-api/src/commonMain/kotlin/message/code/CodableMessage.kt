/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.code

import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.NotStableForInheritance


/**
 * 可以使用 mirai 码表示的 [Message] 类型.
 *
 * 从字符串解析 mirai 码：[MiraiCode.deserializeMiraiCode]
 *
 * ### 不适合第三方实现
 * 即使自定义消息类型实现了接口 [CodableMessage], 在 [MiraiCode.deserializeMiraiCode] 时也无法解析消息类型. 因此实现没有意义.
 *
 * @see MiraiCode
 */
@NotStableForInheritance
public interface CodableMessage : Message {
    /**
     * 转换为 mirai 码.
     */
    public fun serializeToMiraiCode(): String = buildString {
        @OptIn(MiraiExperimentalApi::class)
        appendMiraiCodeTo(this)
    }

    // Using StringBuilder faster than direct plus objects
    @MiraiExperimentalApi
    public fun appendMiraiCodeTo(builder: StringBuilder)
}