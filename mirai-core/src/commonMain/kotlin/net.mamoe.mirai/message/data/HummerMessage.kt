/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils") // since 0.39.1

package net.mamoe.mirai.message.data

import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.PokeMessage.Types
import net.mamoe.mirai.message.data.VipFace.Companion
import net.mamoe.mirai.message.data.VipFace.Kind
import kotlin.jvm.*

/**
 * 一些特殊的消息
 *
 * @see PokeMessage 戳一戳
 * @see FlashImage 闪照
 */
public sealed class HummerMessage : MessageContent {
    public companion object Key : Message.Key<HummerMessage> {
        public override val typeName: String
            get() = "HummerMessage"
    }
    // has service type etc.
}

////////////////////////////////////////
///////////// POKE MESSAGE /////////////
////////////////////////////////////////

/**
 * 戳一戳. 可以发送给好友或群.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:poke:*[name]*,*[type]*,*[id]*&#93;
 *
 * @see Types 使用伴生对象中的常量
 */
public data class PokeMessage internal constructor(
    /**
     * 仅 mirai, 显示的名称
     */
    public val name: String,

    public val type: Int,
    public val id: Int
) : HummerMessage(), CodableMessage {
    @Suppress("DEPRECATION_ERROR", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    public companion object Types : Message.Key<PokeMessage> {
        public override val typeName: String
            get() = "PokeMessage"

        /** 戳一戳 */
        @JvmField
        public val Poke: PokeMessage = PokeMessage("戳一戳", 1, -1)

        /** 比心 */
        @JvmField
        public val ShowLove: PokeMessage = PokeMessage("比心", 2, -1)

        /** 点赞  */
        @JvmField
        public val Like: PokeMessage = PokeMessage("点赞", 3, -1)

        /** 心碎 */
        @JvmField
        public val Heartbroken: PokeMessage = PokeMessage("心碎", 4, -1)

        /** 666 */
        @JvmField
        public val SixSixSix: PokeMessage = PokeMessage("666", 5, -1)

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
        @JvmStatic
        public val values: Array<PokeMessage> = arrayOf(
            Poke, ShowLove, Like, Heartbroken, SixSixSix,
            FangDaZhao, BaoBeiQiu, Rose, ZhaoHuanShu, RangNiPi,
            JieYin, ShouLei, GouYin, ZhuaYiXia, SuiPing
        )
    }


    private val stringValue = "[mirai:poke:$name,$type,$id]"

    override fun toString(): String = stringValue
    override fun contentToString(): String = "[戳一戳]"
    //businessType=0x00000001(1)
    //pbElem=08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00
    //serviceType=0x00000002(2)
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
 * @see Types 使用伴生对象中的常量
 */
public data class VipFace internal constructor(
    /**
     * 使用 [Companion] 中常量.
     */
    public val kind: Kind,
    public val count: Int
) : HummerMessage(), CodableMessage {
    public data class Kind(
        val id: Int,
        val name: String
    ) {
        public override fun toString(): String {
            return "$id,$name"
        }
    }

    @Suppress("DEPRECATION_ERROR", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    public companion object : Message.Key<VipFace> {
        override val typeName: String get() = "VipFace"

        @JvmStatic
        public val LiuLian: Kind = 9 to "榴莲"

        @JvmStatic
        public val PingDiGuo: Kind = 1 to "平底锅"

        @JvmStatic
        public val ChaoPiao: Kind = 12 to "钞票"

        @JvmStatic
        public val LueLueLue: Kind = 10 to "略略略"

        @JvmStatic
        public val ZhuTou: Kind = 4 to "猪头"

        @JvmStatic
        public val BianBian: Kind = 6 to "便便"

        @JvmStatic
        public val ZhaDan: Kind = 5 to "炸弹"

        @JvmStatic
        public val AiXin: Kind = 2 to "爱心"

        @JvmStatic
        public val HaHa: Kind = 3 to "哈哈"

        @JvmStatic
        public val DianZan: Kind = 1 to "点赞"

        @JvmStatic
        public val QinQin: Kind = 7 to "亲亲"

        @JvmStatic
        public val YaoWan: Kind = 8 to "药丸"

        @JvmStatic
        public val values: Array<Kind> = arrayOf(
            LiuLian, PingDiGuo, ChaoPiao, LueLueLue, ZhuTou,
            BianBian, ZhaDan, AiXin, HaHa, DianZan, QinQin, YaoWan
        )

        private infix fun Int.to(name: String): Kind = Kind(this, name)
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
public sealed class FlashImage : MessageContent, HummerMessage(), CodableMessage {
    public companion object Key : Message.Key<FlashImage> {
        /**
         * 将普通图片转换为闪照.
         */
        @JvmStatic
        @JvmName("from")
        public operator fun invoke(image: Image): FlashImage {

            return when (image) {
                is GroupImage -> GroupFlashImage(image)
                is FriendImage -> FriendFlashImage(image)
                else -> throw IllegalArgumentException("不支持的图片类型(Please use GroupImage or FriendImage)")
            }
        }

        /**
         * 将普通图片转换为闪照.
         *
         * @param imageId 图片 id, 详见 [Image.imageId]
         */
        @JvmStatic
        @JvmName("from")
        public operator fun invoke(imageId: String): FlashImage {
            return invoke(Image(imageId))
        }

        public override val typeName: String
            get() = "FlashImage"
    }

    /**
     * 闪照的内容图片, 即一个普通图片.
     */
    public abstract val image: Image

    private var stringValue: String? = null
        get() {
            return field ?: kotlin.run {
                field = "[mirai:flash:${image.imageId}]"
                field
            }
        }

    public final override fun toString(): String = stringValue!!
    public override fun contentToString(): String = "[闪照]"
}

public inline fun Image.flash(): FlashImage = FlashImage(this)

@JvmSynthetic
public inline fun GroupImage.flash(): GroupFlashImage = FlashImage(this) as GroupFlashImage

@JvmSynthetic
public inline fun FriendImage.flash(): FriendFlashImage = FlashImage(this) as FriendFlashImage

/**
 * @see FlashImage.invoke
 */
public data class GroupFlashImage(public override val image: GroupImage) : FlashImage() {
    public companion object Key : Message.Key<GroupFlashImage> {
        public override val typeName: String
            get() = "GroupFlashImage"
    }
}

/**
 * @see FlashImage.invoke
 */
public data class FriendFlashImage(public override val image: FriendImage) : FlashImage() {
    public companion object Key : Message.Key<FriendFlashImage> {
        public override val typeName: String
            get() = "FriendFlashImage"
    }
}
