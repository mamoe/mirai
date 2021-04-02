/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast
import org.jetbrains.annotations.Range
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * 骰子.
 *
 * 构造 [Dice] 实例即可使用. 也可以通过 [Dice.random] 获得一个随机点数的实例.
 *
 * @since 2.5
 */
@Serializable
@SerialName(Dice.SERIAL_NAME)
public data class Dice(
    /**
     * 骰子的点数. 范围为 1..6
     */
    public val value: @Range(from = 1, to = 6) Int
) : MarketFace, CodableMessage {
    init {
        require(value in 1..6) { "Dice.value must be in 1 and 6 inclusive." }
    }

    @MiraiExperimentalApi
    override val name: String
        get() = "[骰子:$value]"

    @MiraiExperimentalApi
    override val id: Int
        get() = 11464

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:dice:").append(value).append(']')
    }

    override fun toString(): String = "[mirai:dice:$value]"

    public companion object Key :
        AbstractPolymorphicMessageKey<MarketFace, Dice>(MarketFace, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "Dice"

        /**
         * 创建随机点数的 [骰子][Dice]
         */
        @JvmStatic
        public fun random(): Dice = Dice(Random.nextInt(1..6))
    }
}
