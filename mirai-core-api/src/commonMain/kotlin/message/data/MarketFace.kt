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
import kotlin.random.Random

/**
 * 商城表情
 *
 * 目前仅支持直接发送骰子表情，其余表情可保存接收到的来自官方客户端的商城表情然后转发.
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

        /**
         * 生成骰子表情.
         *
         * [value] 需要生成的数字，默认为随机生成
         */
        public fun dice(value: Int = Random.nextInt(1, 7)): MarketFace {
            require(value in 1..6) {
                "Dice value must in 1 to 6"
            }
            return Dice(value)
        }
    }
}

/**
 * 用于标识可发送的商城表情
 */
public interface MarketFaceTemplate

/**
 * 骰子表情
 *
 * 生成请参见 [MarketFace.Key.dice]
 */
public class Dice internal constructor(public val value: Int) : MarketFace, MarketFaceTemplate {
    override val id: Int get() = 11464
    override fun toString(): String = "[mirai:marketface:$id,$name]"

    override val name: String get() = "[骰子:$value]"
}