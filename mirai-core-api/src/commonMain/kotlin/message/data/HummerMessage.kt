/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils") // since 0.39.1

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.castOrNull

/**
 * 一些特殊的消息
 *
 * 注意, [HummerMessage] 类型不稳定, 但它的子类如 [PokeMessage] 是稳定的.
 *
 * @see PokeMessage 戳一戳
 * @see FlashImage 闪照
 * @see MarketFace 商城表情
 * @see VipFace VIP 表情
 */
@MiraiExperimentalApi
public interface HummerMessage : MessageContent, ConstrainSingle {
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, HummerMessage>(MessageContent, { it.castOrNull() })
    // has service type etc.
}