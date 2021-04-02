/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendStringAsMiraiCode
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.castOrNull

/**
 * 戳一戳. 可以发送给好友或群.
 *
 * 备注: 这是消息对话框中显示的 "一个手指" 的戳一戳. 类似微信拍一拍的是 [Nudge].
 *
 * 使用 [PokeMessage] 的静态字段, 而不要手动构造 [PokeMessage] 实例.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:poke:*[name]*,*[pokeType]*,*[id]*&#93;
 *
 * @see PokeMessage.Companion 使用伴生对象中的常量
 */
@SerialName(PokeMessage.SERIAL_NAME)
@Serializable
public data class PokeMessage @MiraiInternalApi constructor(
    /**
     * 仅 mirai, 显示的名称
     */
    public val name: String,

    public val pokeType: Int, // 'type' is used by serialization
    public val id: Int
) : HummerMessage, CodableMessage {
    override val key: MessageKey<HummerMessage> get() = Key

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:poke:").appendStringAsMiraiCode(name)
            .append(',').append(pokeType).append(',').append(id)
            .append(']')
    }

    override fun toString(): String = "[mirai:poke:$name,$pokeType,$id]"
    override fun contentToString(): String = "[戳一戳]"
    //businessType=0x00000001(1)
    //pbElem=08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00
    //serviceType=0x00000002(2)


    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, PokeMessage>(HummerMessage, { it.castOrNull() }) {

        public const val SERIAL_NAME: String = "PokeMessage"

        /** 戳一戳 */
        @JvmField
        public val ChuoYiChuo: PokeMessage = PokeMessage("戳一戳", 1, -1)

        /** 比心 */
        @JvmField
        public val BiXin: PokeMessage = PokeMessage("比心", 2, -1)

        /** 点赞  */
        @JvmField
        public val DianZan: PokeMessage = PokeMessage("点赞", 3, -1)

        /** 心碎 */
        @JvmField
        public val XinSui: PokeMessage = PokeMessage("心碎", 4, -1)

        /** 666 */
        @JvmField
        public val LiuLiuLiu: PokeMessage = PokeMessage("666", 5, -1)

        /** 放大招 */
        @JvmField
        public val FangDaZhao: PokeMessage = PokeMessage("放大招", 6, -1)

        /** 宝贝球 (SVIP) */
        @JvmField
        public val BaoBeiQiu: PokeMessage = PokeMessage("宝贝球", 126, 2011)

        /** 玫瑰花 (SVIP) */
        @JvmField
        public val Rose: PokeMessage = PokeMessage("玫瑰花", 126, 2007)

        /** 召唤术 (SVIP) */
        @JvmField
        public val ZhaoHuanShu: PokeMessage = PokeMessage("召唤术", 126, 2006)

        /** 让你皮 (SVIP) */
        @JvmField
        public val RangNiPi: PokeMessage = PokeMessage("让你皮", 126, 2009)

        /** 结印 (SVIP) */
        @JvmField
        public val JieYin: PokeMessage = PokeMessage("结印", 126, 2005)

        /** 手雷 (SVIP) */
        @JvmField
        public val ShouLei: PokeMessage = PokeMessage("手雷", 126, 2004)

        /** 勾引 */
        @JvmField
        public val GouYin: PokeMessage = PokeMessage("勾引", 126, 2003)

        /** 抓一下 (SVIP) */
        @JvmField
        public val ZhuaYiXia: PokeMessage = PokeMessage("抓一下", 126, 2001)

        /** 碎屏 (SVIP) */
        @JvmField
        public val SuiPing: PokeMessage = PokeMessage("碎屏", 126, 2002)

        /** 敲门 (SVIP) */
        @JvmField
        public val QiaoMen: PokeMessage = PokeMessage("敲门", 126, 2002)


        /**
         * 所有类型数组
         */
        @JvmField
        public val values: Array<PokeMessage> = arrayOf(
            ChuoYiChuo, BiXin, DianZan, XinSui, LiuLiuLiu,
            FangDaZhao, BaoBeiQiu, Rose, ZhaoHuanShu, RangNiPi,
            JieYin, ShouLei, GouYin, ZhuaYiXia, SuiPing
        )
    }
}