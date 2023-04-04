/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.RockPaperScissors.*
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*
import kotlin.jvm.*
import kotlin.random.Random

/**
 * 石头剪刀布.
 *
 * 可以通过 [RockPaperScissors.random] 获得一个随机手势的实例.
 *
 * @property ROCK 石头 `[mirai:rps:rock]`
 * @property SCISSORS 剪刀 `[mirai:rps:scissors]`
 * @property PAPER 布（纸）`[mirai:rps:paper]`
 *
 * @since 2.14
 */
@kotlin.Suppress("RemoveRedundantQualifierName")
@Serializable(RockPaperScissors.Serializer::class)
@SerialName(RockPaperScissors.SERIAL_NAME)
public enum class RockPaperScissors(
    public val content: String,

    internalId: Int,
) : MarketFace, CodableMessage {
    ROCK("[石头]", 48),
    SCISSORS("[剪刀]", 49),
    PAPER("[布]", 50)
    ;

    @MiraiExperimentalApi
    override val id: Int
        get() = 11415

    @MiraiInternalApi
    @JvmSynthetic
    public val internalId: Byte = internalId.toByte()

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:rps:").append(name.lowercase()).append(']')
    }

    override fun toString(): String = serializeToMiraiCode()


    override fun contentToString(): String = content

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitRockPaperScissors(this, data)
    }

    /**
     * 判断 当前手势 (`this`) 能否淘汰对手 ([other])
     *
     * @return 赢返回 `true`，输返回 `false`，平局时返回 `null`
     */
    public infix fun eliminates(other: RockPaperScissors): Boolean? {
        return when {
            this == other -> null
            this == ROCK && other == SCISSORS -> true
            this == SCISSORS && other == PAPER -> true
            this == PAPER && other == ROCK -> true
            else -> false
        }
    }

    public companion object Key :
        AbstractPolymorphicMessageKey<MarketFace, RockPaperScissors>(MarketFace, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "RockPaperScissors"

        private val values = values()

        /**
         * 获取随机手势的 [石头剪刀布][RockPaperScissors]
         *
         * Java 可通过 `kotlin.random.PlatformRandomKt.asKotlinRandom()` 来传入一个 random
         */
        @JvmStatic
        @JvmOverloads
        public fun random(random: Random = Random): RockPaperScissors = RockPaperScissors.values.random(random)

    }

    internal object Serializer : KSerializer<RockPaperScissors> by Surrogate.serializer().map(
        resultantDescriptor = Surrogate.serializer().descriptor,
        deserialize = { valueOf(it.name) },
        serialize = { Surrogate(name) },
    ) {

        @Serializable
        @SerialName(RockPaperScissors.SERIAL_NAME)
        private class Surrogate(
            val name: String,
        )
    }
}
