/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast

/**
 * 闪照. 闪照的内容取决于 [image] 代表的图片.
 *
 * ## 构造闪照
 *
 * 需要首先获得普通图片才能构造闪照. 详见 [Image].
 *
 * - 使用 [FlashImage.from] 将普通图片转换为闪照.
 * - 在 Kotlin 使用类构造器顶层函数 `FlashImage(image)`.
 * - 在 Kotlin 使用扩展 [Image.flash].
 *
 * ## 获得闪照代表的原图片
 *
 * 访问属性 [FlashImage.image]
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:flash:*[Image.imageId]*&#93;
 *
 * @see Image 查看图片相关信息
 */
@Serializable
@SerialName(FlashImage.SERIAL_NAME)
public data class FlashImage(
    /**
     * 闪照的内容图片, 即一个普通图片.
     */
    @SerialName("imageId")
    @Serializable(Image.AsStringSerializer::class)
    public val image: Image
) : MessageContent, HummerMessage, CodableMessage, ConstrainSingle {
    override val key: MessageKey<FlashImage> get() = Key

    private val stringValue: String by lazy(LazyThreadSafetyMode.NONE) { "[mirai:flash:${image.imageId}]" }

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append(stringValue)
    }

    override fun serializeToMiraiCode(): String = stringValue
    override fun toString(): String = stringValue
    override fun contentToString(): String = "[闪照]"

    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, FlashImage>(HummerMessage, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "FlashImage"

        /**
         * 将普通图片转换为闪照.
         *
         * @param imageId 图片 id, 详见 [Image.imageId]
         */
        @JvmStatic
        public fun from(imageId: String): FlashImage = FlashImage(Image(imageId))

        /**
         * 将普通图片转换为闪照.
         *
         * @see Image.flash
         */
        @JvmStatic
        public inline fun from(image: Image): FlashImage = FlashImage(image)
    }
}

/**
 * 将普通图片转换为闪照.
 */
@JvmSynthetic
public inline fun FlashImage(imageId: String): FlashImage = FlashImage.from(imageId)

/**
 * 将普通图片转换为闪照.
 */
@JvmSynthetic
public inline fun Image.flash(): FlashImage = FlashImage(this)