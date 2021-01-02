/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils") // since 0.39.1

package net.mamoe.mirai.message.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendAsMiraiCode
import net.mamoe.mirai.message.data.VipFace.Kind
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.castOrNull
import net.mamoe.mirai.utils.safeCast

/**
 * 一些特殊的消息
 *
 * @see PokeMessage 戳一戳
 * @see FlashImage 闪照
 */
public interface HummerMessage : MessageContent, ConstrainSingle {
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, HummerMessage>(MessageContent, { it.castOrNull() })
    // has service type etc.
}

////////////////////////////////////////
///////////// POKE MESSAGE /////////////
////////////////////////////////////////

/**
 * 戳一戳. 可以发送给好友或群.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:poke:*[name]*,*[pokeType]*,*[id]*&#93;
 *
 * @see PokeMessage.Companion 使用伴生对象中的常量
 */
@Serializable
public data class PokeMessage @MiraiInternalApi constructor(
    /**
     * 仅 mirai, 显示的名称
     */
    public val name: String,

    public val pokeType: Int, // 'type' is used by serialization
    public val id: Int
) : HummerMessage, CodableMessage {
    override val key: MessageKey<HummerMessage>
        get() = Key


    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, PokeMessage>(HummerMessage, { it.castOrNull() }) {

        /** 戳一戳 */
        @JvmField
        public val ChuoYiChuo: PokeMessage = PokeMessage("戳一戳", 1, -1)

        /** 戳一戳 */
        @JvmField
        @Deprecated("Use ChuoYiChuo", replaceWith = ReplaceWith("ChuoYiChuo"))
        public val Poke: PokeMessage = ChuoYiChuo

        /** 比心 */
        @JvmField
        public val BiXin: PokeMessage = PokeMessage("比心", 2, -1)

        /** 比心 */
        @JvmField
        @Deprecated("Use BiXin", replaceWith = ReplaceWith("BiXin"))
        public val ShowLove: PokeMessage = BiXin

        /** 点赞  */
        @JvmField
        public val DianZan: PokeMessage = PokeMessage("点赞", 3, -1)

        /** 点赞 */
        @JvmField
        @Deprecated("Use DianZan", replaceWith = ReplaceWith("DianZan"))
        public val Like: PokeMessage = DianZan

        /** 心碎 */
        @JvmField
        public val XinSui: PokeMessage = PokeMessage("心碎", 4, -1)

        /** 心碎 */
        @JvmField
        @Deprecated("Use XinSui", replaceWith = ReplaceWith("XinSui"))
        public val Heartbroken: PokeMessage = XinSui

        /** 666 */
        @JvmField
        public val LiuLiuLiu: PokeMessage = PokeMessage("666", 5, -1)

        /** 666 */
        @JvmField
        @Deprecated("Use LiuLiuLiu", replaceWith = ReplaceWith("LiuLiuLiu"))
        public val SixSixSix: PokeMessage = LiuLiuLiu

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


    private val stringValue = "[mirai:poke:$name,$pokeType,$id]"

    override fun appendMiraiCode(builder: StringBuilder) {
        builder.append("[mirai:poke:").appendAsMiraiCode(name)
            .append(',').append(pokeType).append(',').append(id)
            .append(']')
    }

    override fun toString(): String = stringValue
    override fun contentToString(): String = "[戳一戳]"
    //businessType=0x00000001(1)
    //pbElem=08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00
    //serviceType=0x00000002(2)
}
////////////////////////////////////
////////// MARKET FACE /////////////
////////////////////////////////////
/**
 * 商城表情
 *
 * 目前不支持直接发送，可支持转发，但其取决于表情是否可使用.
 */
public interface MarketFace : HummerMessage {
    public val name: String
    public val id: Int

    override val key: MessageKey<MarketFace>
        get() = Key

    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, MarketFace>(HummerMessage, { it.safeCast() })

    override fun contentToString(): String = name
}


////////////////////////////////////
///////////// VIP FACE /////////////
////////////////////////////////////

/**
 * VIP 表情.
 *
 * 不支持发送.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:vipface:*[Kind.id]*,*[Kind.name]*,*[count]*&#93;
 *
 * @see VipFace.Key 使用伴生对象中的常量
 */
@Serializable
public data class VipFace @MiraiInternalApi constructor(
    /**
     * 使用 [Companion] 中常量.
     */
    public val kind: Kind,
    public val count: Int
) : HummerMessage, CodableMessage {
    @Serializable
    public data class Kind(
        val id: Int,
        val name: String
    ) {
        public override fun toString(): String {
            return "$id,$name"
        }
    }

    override val key: MessageKey<VipFace>
        get() = Key

    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, VipFace>(HummerMessage, { it.safeCast() }) {

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

        private infix fun Int.to(name: String): Kind = Kind(this, name)
    }

    override fun appendMiraiCode(builder: StringBuilder) {
        builder.append(stringValue)
    }

    private val stringValue = "[mirai:vipface:$kind,$count]"

    override fun toString(): String = stringValue
    override fun contentToString(): String = "[${kind.name}]x$count"
}


///////////////////////////////////////
///////////// FLASH IMAGE /////////////
///////////////////////////////////////


/**
 * 闪照
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:flash:*[Image.imageId]*&#93;
 *
 * @see Image.flash 转换普通图片为闪照
 *
 * @see Image 查看图片相关信息
 */
@Serializable
public data class FlashImage(
    /**
     * 闪照的内容图片, 即一个普通图片.
     */
    @Contextual
    public val image: Image
) : MessageContent, HummerMessage, CodableMessage, ConstrainSingle {
    override val key: MessageKey<FlashImage>
        get() = Key

    public companion object Key :
        AbstractPolymorphicMessageKey<HummerMessage, FlashImage>(HummerMessage, { it.safeCast() }) {

        /**
         * 将普通图片转换为闪照.
         *
         * @param imageId 图片 id, 详见 [Image.imageId]
         */
        @JvmStatic
        public fun from(imageId: String): FlashImage = FlashImage(Image(imageId))
    }

    private val stringValue: String by lazy(LazyThreadSafetyMode.NONE) { "[mirai:flash:${image.imageId}]" }

    override fun appendMiraiCode(builder: StringBuilder) {
        builder.append(stringValue)
    }

    override fun toMiraiCode(): String = stringValue
    public override fun toString(): String = stringValue
    public override fun contentToString(): String = "[闪照]"
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

/**
 * 将普通图片转换为闪照.
 */
@JvmSynthetic
public inline fun GroupImage.flash(): FlashImage = FlashImage(this)

/**
 * 将普通图片转换为闪照.
 */
@JvmSynthetic
public inline fun FriendImage.flash(): FlashImage = FlashImage(this)