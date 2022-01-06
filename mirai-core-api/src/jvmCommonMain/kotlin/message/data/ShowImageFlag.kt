/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import net.mamoe.mirai.utils.safeCast

/**
 * [MessageChain] 中包含秀图时的标记.
 *
 * 秀图已被 QQ 弃用, 仅作识别处理
 *
 *
 * ```
 * MessageEvent event;
 *
 * if (event.message.contains(ShowImageFlag.INSTANCE)) {
 *     // event.message 包含的图片是作为 '秀图' 发送
 * }
 * ```
 *
 * 发送 [ShowImageFlag] 不会有任何效果.
 *
 * @since 2.2
 */
@SerialName(ShowImageFlag.SERIAL_NAME)
@Serializable(ShowImageFlag.Serializer::class)
public object ShowImageFlag : MessageMetadata, ConstrainSingle, AbstractMessageKey<ShowImageFlag>({ it.safeCast() }) {
    override val key: ShowImageFlag get() = this

    override fun toString(): String = "ShowImageFlag"

    /**
     * @since 2.4
     */
    public const val SERIAL_NAME: String = "ShowImageFlag"

    /**
     * @since 2.4
     */
    internal object Serializer : KSerializer<ShowImageFlag> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(SERIAL_NAME)

        override fun deserialize(decoder: Decoder): ShowImageFlag {
            decoder.decodeStructure(descriptor) {}
            return ShowImageFlag
        }


        override fun serialize(encoder: Encoder, value: ShowImageFlag) {
            encoder.encodeStructure(descriptor) {}
        }
    }
}
