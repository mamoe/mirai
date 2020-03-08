/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai

/**
 * 小程序, 如音乐分享
 */
@OptIn(MiraiExperimentalAPI::class)
@SinceMirai("0.27.0")
class LightApp constructor(override val content: String) : RichMessage {
    companion object Key : Message.Key<LightApp>

    override fun toString(): String = content
}