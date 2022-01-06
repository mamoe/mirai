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
import net.mamoe.mirai.message.data.VipFace.Kind
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.safeCast

/**
 * VIP 表情.
 *
 * 不支持发送, 在发送时会变为纯文本.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:vipface:*[Kind.id]*,*[Kind.name]*,*[count]*&#93;
 *
 * @see VipFace.Key 使用伴生对象中的常量
 */
@Serializable
@SerialName(VipFace.SERIAL_NAME)
public data class VipFace @MiraiInternalApi constructor(
    /**
     * 使用 [Companion] 中常量.
     */
    public val kind: Kind,
    public val count: Int
) : HummerMessage, CodableMessage {
    override val key: MessageKey<VipFace> get() = Key

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:vipface:").append(kind).append(',').append(count).append(']')
    }

    override fun toString(): String = "[mirai:vipface:$kind,$count]"
    override fun contentToString(): String = "[${kind.name}]x$count"

    @Serializable
    public data class Kind(
        val id: Int,
        val name: String
    ) {
        public override fun toString(): String {
            return "$id,$name"
        }
    }

    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, VipFace>(HummerMessage, { it.safeCast() }) {

        public const val SERIAL_NAME: String = "VipFace"

        @JvmField
        public val LiuLian: Kind = 9 to "榴莲"

        @JvmField
        public val PingDiGuo: Kind = 1 to "平底锅"

        @JvmField
        public val ChaoPiao: Kind = 12 to "钞票"

        @JvmField
        public val LueLueLue: Kind = 10 to "略略略"

        @JvmField
        public val ZhuTou: Kind = 4 to "猪头"

        @JvmField
        public val BianBian: Kind = 6 to "便便"

        @JvmField
        public val ZhaDan: Kind = 5 to "炸弹"

        @JvmField
        public val AiXin: Kind = 2 to "爱心"

        @JvmField
        public val HaHa: Kind = 3 to "哈哈"

        @JvmField
        public val DianZan: Kind = 1 to "点赞"

        @JvmField
        public val QinQin: Kind = 7 to "亲亲"

        @JvmField
        public val YaoWan: Kind = 8 to "药丸"

        @JvmField
        public val values: Array<Kind> = arrayOf(
            LiuLian, PingDiGuo, ChaoPiao, LueLueLue, ZhuTou,
            BianBian, ZhaDan, AiXin, HaHa, DianZan, QinQin, YaoWan
        )

        private inline infix fun Int.to(name: String): Kind = Kind(this, name)
    }
}