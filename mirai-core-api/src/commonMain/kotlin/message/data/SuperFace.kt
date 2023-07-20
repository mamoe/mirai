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
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.safeCast
import kotlin.jvm.Throws

/**
 * 超级表情
 *
 * @see Face
 */
@OptIn(MiraiExperimentalApi::class)
@Serializable
@SerialName(SuperFace.SERIAL_NAME)
@NotStableForInheritance
public data class SuperFace @MiraiInternalApi constructor(
    public val face: Int,
    public val id: String,
    @SerialName("sticker_type")
    public val type: Int
) : HummerMessage, CodableMessage {

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, SuperFace>(
            MessageContent,
            { it.safeCast() }) {

        public const val SERIAL_NAME: String = "SuperFace"

        /**
         * 将普通表情转换为超级表情.
         **/
        @JvmStatic
        @OptIn(MiraiInternalApi::class)
        @Throws(UnsupportedOperationException::class)
        public fun from(face: Face): SuperFace {
            val stickerId = when (face.id) {
                Face.DA_CALL -> "1"
                Face.BIAN_XING -> "2"
                Face.KE_DAO_LE -> "3"
                Face.ZI_XI_FEN_XI -> "4"
                Face.JIA_YOU -> "5"
                Face.WO_MEI_SHI -> "6"
                Face.CAI_GOU -> "7"
                Face.CHONG_BAI -> "8"
                Face.BI_XIN -> "9"
                Face.QING_ZHU -> "10"
                Face.LAO_SE_PI -> "11"
                Face.CHI_TANG -> "12"
                Face.LAN_QIU -> "13"
                Face.JING_XIA -> "14"
                Face.SHENG_QI -> "15"
                Face.LIU_LEI -> "16"
                Face.DAN_GAO -> "17"
                Face.BIAN_PAO -> "18"
                Face.YAN_HUA -> "19"
                Face.WO_XIANG_KAI_LE -> "20"
                Face.TIAN_PING -> "21"
                Face.HUA_DUO_LIAN -> "22"
                Face.RE_HUA_LE -> "23"
                Face.DA_ZHAO_HU -> "24"
                Face.NI_ZHEN_BANG_BANG -> "25"
                Face.SUAN_Q -> "26"
                Face.WO_FANG_LE -> "27"
                Face.DA_YUAN_ZHONG -> "28"
                Face.HONG_BAO_DUO_DUO -> "29"
                else -> throw UnsupportedOperationException(face.name)
            }
            val stickerType = when (face.id) {
                Face.LAN_QIU -> 2
                else -> 1
            }

            return SuperFace(face = face.id, id = stickerId, type = stickerType)
        }
    }

    override val key: MessageKey<SuperFace> get() = Key

    public val name: String get() = contentToString().let { it.substring(1, it.length - 1) }

    override fun toString(): String = "[mirai:superface:$face,$id,$type]"

    override fun contentToString(): String = Face.names.getOrElse(face) { "[超级表情]" }

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:superface:").append(face).append(',').append(id).append(',').append(type).append(']')
    }

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitSuperFace(this, data)
    }
}

@JvmSynthetic
public fun SuperFace.toFace(): Face = Face(id = face)

@JvmSynthetic
public fun SuperFace(face: Face): SuperFace = SuperFace.from(face)

@JvmSynthetic
public fun SuperFace(face: Int): SuperFace = SuperFace.from(face = Face(id = face))