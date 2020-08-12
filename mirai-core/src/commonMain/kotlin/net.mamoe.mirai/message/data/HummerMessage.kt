/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
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
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.jvm.*

/**
 * 一些特殊的消息
 *
 * @see PokeMessage 戳一戳
 * @see FlashImage 闪照
 */
sealed class HummerMessage : MessageContent {
    companion object Key : Message.Key<HummerMessage> {
        override val typeName: String
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
data class PokeMessage internal constructor(
    /**
     * 仅 mirai, 显示的名称
     */
    val name: String,

    val type: Int,
    val id: Int
) : HummerMessage(), CodableMessage {
    @Suppress("DEPRECATION_ERROR", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    companion object Types : Message.Key<PokeMessage> {
        override val typeName: String
            get() = "PokeMessage"

        /** 戳一戳 */
        @JvmField
        val Poke = PokeMessage("戳一戳", 1, -1)

        /** 比心 */
        @JvmField
        val ShowLove = PokeMessage("比心", 2, -1)

        /** 点赞  */
        @JvmField
        val Like = PokeMessage("点赞", 3, -1)

        /** 心碎 */
        @JvmField
        val Heartbroken = PokeMessage("心碎", 4, -1)

        /** 666 */
        @JvmField
        val SixSixSix = PokeMessage("666", 5, -1)

        /** 放大招 */
        @JvmField
        val FangDaZhao = PokeMessage("放大招", 6, -1)

        /** 宝贝球 (SVIP) */
        @JvmField
        val BaoBeiQiu = PokeMessage("宝贝球", 126, 2011)

        /** 玫瑰花 (SVIP) */
        @JvmField
        val Rose = PokeMessage("玫瑰花", 126, 2007)

        /** 召唤术 (SVIP) */
        @JvmField
        val ZhaoHuanShu = PokeMessage("召唤术", 126, 2006)

        /** 让你皮 (SVIP) */
        @JvmField
        val RangNiPi = PokeMessage("让你皮", 126, 2009)

        /** 结印 (SVIP) */
        @JvmField
        val JieYin = PokeMessage("结印", 126, 2005)

        /** 手雷 (SVIP) */
        @JvmField
        val ShouLei = PokeMessage("手雷", 126, 2004)

        /** 勾引 */
        @JvmField
        val GouYin = PokeMessage("勾引", 126, 2003)

        /** 抓一下 (SVIP) */
        @JvmField
        val ZhuaYiXia = PokeMessage("抓一下", 126, 2001)

        /** 碎屏 (SVIP) */
        @JvmField
        val SuiPing = PokeMessage("碎屏", 126, 2002)

        /** 敲门 (SVIP) */
        @JvmField
        val QiaoMen = PokeMessage("敲门", 126, 2002)


        /**
         * 所有类型数组
         */
        @JvmStatic
        val values: Array<PokeMessage> = arrayOf(
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
data class VipFace internal constructor(
    /**
     * 使用 [Companion] 中常量.
     */
    val kind: Kind,
    val count: Int
) : HummerMessage(), CodableMessage {
    data class Kind(
        val id: Int,
        val name: String
    ) {
        override fun toString(): String {
            return "$id,$name"
        }
    }

    @Suppress("DEPRECATION_ERROR", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    companion object : Message.Key<VipFace> {
        override val typeName: String get() = "VipFace"

        @JvmStatic
        val LiuLian = 9 to "榴莲"

        @JvmStatic
        val PingDiGuo = 1 to "平底锅"

        @JvmStatic
        val ChaoPiao = 12 to "钞票"

        @JvmStatic
        val LueLueLue = 10 to "略略略"

        @JvmStatic
        val ZhuTou = 4 to "猪头"

        @JvmStatic
        val BianBian = 6 to "便便"

        @JvmStatic
        val ZhaDan = 5 to "炸弹"

        @JvmStatic
        val AiXin = 2 to "爱心"

        @JvmStatic
        val HaHa = 3 to "哈哈"

        @JvmStatic
        val DianZan = 1 to "点赞"

        @JvmStatic
        val QinQin = 7 to "亲亲"

        @JvmStatic
        val YaoWan = 8 to "药丸"

        @JvmStatic
        val values: Array<Kind> = arrayOf(
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
sealed class FlashImage : MessageContent, HummerMessage(), CodableMessage {
    companion object Key : Message.Key<FlashImage> {
        /**
         * 将普通图片转换为闪照.
         */
        @JvmStatic
        @JvmName("from")
        operator fun invoke(image: Image): FlashImage {

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
        operator fun invoke(imageId: String): FlashImage {
            return invoke(Image(imageId))
        }

        override val typeName: String
            get() = "FlashImage"
    }

    /**
     * 闪照的内容图片, 即一个普通图片.
     */
    abstract val image: Image

    private var stringValue: String? = null
        get() {
            return field ?: kotlin.run {
                field = "[mirai:flash:${image.imageId}]"
                field
            }
        }

    final override fun toString(): String = stringValue!!
    override fun contentToString(): String = "[闪照]"
}
inline fun Image.flash(): FlashImage = FlashImage(this)

@JvmSynthetic
inline fun GroupImage.flash(): GroupFlashImage = FlashImage(this) as GroupFlashImage

@JvmSynthetic
inline fun FriendImage.flash(): FriendFlashImage = FlashImage(this) as FriendFlashImage

/**
 * @see FlashImage.invoke
 */
data class GroupFlashImage(override val image: GroupImage) : FlashImage() {
    companion object Key : Message.Key<GroupFlashImage> {
        override val typeName: String
            get() = "GroupFlashImage"
    }
}

/**
 * @see FlashImage.invoke
 */
data class FriendFlashImage(override val image: FriendImage) : FlashImage() {
    companion object Key : Message.Key<FriendFlashImage> {
        override val typeName: String
            get() = "FriendFlashImage"
    }
}
