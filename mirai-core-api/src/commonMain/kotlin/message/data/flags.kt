/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.safeCast

/**
 * [MessageChain] 中包含秀图时的标记
 *
 * 秀图已被 QQ 弃用, 仅作识别处理
 *
 * @since 2.2
 */
public object ShowImageFlag : MessageMetadata, ConstrainSingle, AbstractMessageKey<ShowImageFlag>({ it.safeCast() }) {
    override val key: ShowImageFlag get() = this

    override fun toString(): String = "ShowImageFlag"
}
