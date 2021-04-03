/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast

/**
 * 商城表情
 *
 * 除 [Dice] 可以发送外, 目前不支持直接构造和发送，可保存接收到的来自官方客户端的商城表情然后转发.
 *
 * @see Dice
 */
public interface MarketFace : HummerMessage {
    /**
     * 如 `[开心]`
     */
    public val name: String

    /**
     * 内部 id.
     */
    @MiraiExperimentalApi
    public val id: Int

    override val key: MessageKey<MarketFace> get() = Key

    override fun contentToString(): String = name

    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, MarketFace>(HummerMessage, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "MarketFace"
    }
}