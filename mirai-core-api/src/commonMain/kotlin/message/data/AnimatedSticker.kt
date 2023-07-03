/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.safeCast

/**
 * 动画表情
 *
 * @see Face
 */
@OptIn(MiraiExperimentalApi::class)
@Serializable
@SerialName(AnimatedSticker.SERIAL_NAME)
@NotStableForInheritance
public data class AnimatedSticker(
    public val id: Int
) : HummerMessage {

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, AnimatedSticker>(
            MessageContent,
            { it.safeCast() }) {

        public const val SERIAL_NAME: String = "AnimatedSticker"

        /**
         * 将普通表情转换为动画.
         *
         * @see Image.flash
         */
        @JvmStatic
        public fun from(face: Face): AnimatedSticker = AnimatedSticker(face.id)
    }

    override val key: MessageKey<AnimatedSticker> get() = Key

    public val name: String get() = contentToString().let { it.substring(1, it.length - 1) }

    override fun toString(): String = contentToString()

    override fun contentToString(): String = Face.names.getOrElse(id) { "[动画表情]" }
}

/**
 * 将普通表情转换为动画.
 */
@JvmSynthetic
public inline fun Face.animated(): AnimatedSticker = AnimatedSticker.from(this)