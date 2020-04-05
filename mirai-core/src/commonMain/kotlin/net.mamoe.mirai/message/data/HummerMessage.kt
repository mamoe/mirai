/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 一些特殊的消息
 *
 * @see PokeMessage 戳一戳
 * @see FlashImage 闪照
 */
@SinceMirai("0.31.0")
sealed class HummerMessage : MessageContent {
    companion object Key : Message.Key<HummerMessage>
    // has service type etc.
}

////////////////////////////////////////
///////////// POKE MESSAGE /////////////
////////////////////////////////////////

/**
 * 戳一戳. 可以发送给好友或群.
 */
@SinceMirai("0.31.0")
@OptIn(MiraiInternalAPI::class)
class PokeMessage @MiraiInternalAPI(message = "使用伴生对象中的常量") constructor(
    @MiraiExperimentalAPI
    val type: Int,
    @MiraiExperimentalAPI
    val id: Int
) : HummerMessage() {
    companion object Types : Message.Key<PokeMessage> {
        /** 戳一戳 */
        @JvmField
        val Poke = PokeMessage(1, -1)

        /** 比心 */
        @JvmField
        val ShowLove = PokeMessage(2, -1)

        /** 点赞  */
        @JvmField
        val Like = PokeMessage(3, -1)

        /** 心碎 */
        @JvmField
        val Heartbroken = PokeMessage(4, -1)

        /** 666 */
        @JvmField
        val SixSixSix = PokeMessage(5, -1)

        /** 放大招 */
        @JvmField
        val FangDaZhao = PokeMessage(6, -1)
    }

    @OptIn(MiraiExperimentalAPI::class)
    private val stringValue = "[mirai:poke:$type,$id]"

    override fun toString(): String = stringValue
    override val length: Int get() = stringValue.length
    override fun get(index: Int): Char = stringValue[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        stringValue.subSequence(startIndex, endIndex)

    override fun contentToString(): String {
        return "[戳一戳]"
    }

    override fun compareTo(other: String): Int = stringValue.compareTo(other)

    //businessType=0x00000001(1)
    //pbElem=08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00
    //serviceType=0x00000002(2)
}


///////////////////////////////////////
///////////// FLASH IMAGE /////////////
///////////////////////////////////////


/**
 * 闪照
 *
 * @see Image.flash 转换普通图片为闪照
 */
@SinceMirai("0.33.0")
sealed class FlashImage : MessageContent, HummerMessage() {
    companion object Key : Message.Key<FlashImage> {
        /**
         * 将普通图片转换为闪照.
         */
        @JvmStatic
        @JvmName("from")
        operator fun invoke(image: Image): FlashImage {
            @OptIn(MiraiInternalAPI::class)
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

    override fun toString(): String = stringValue!!
    override val length: Int get() = stringValue!!.length
    override fun get(index: Int) = stringValue!![index]
    override fun subSequence(startIndex: Int, endIndex: Int) = stringValue!!.subSequence(startIndex, endIndex)
    override fun compareTo(other: String) = other.compareTo(stringValue!!)

    override fun contentToString(): String = "[闪照]"
}

@SinceMirai("0.33.0")
inline fun Image.flash(): FlashImage = FlashImage(this)

@JvmSynthetic
@SinceMirai("0.33.0")
inline fun GroupImage.flash(): GroupFlashImage = FlashImage(this) as GroupFlashImage

@JvmSynthetic
@SinceMirai("0.33.0")
inline fun FriendImage.flash(): FriendFlashImage = FlashImage(this) as FriendFlashImage

/**
 * @see FlashImage.invoke
 */
@SinceMirai("0.33.0")
class GroupFlashImage(override val image: GroupImage) : FlashImage() {
    companion object Key : Message.Key<GroupFlashImage>
}

/**
 * @see FlashImage.invoke
 */
@SinceMirai("0.33.0")
class FriendFlashImage(override val image: FriendImage) : FlashImage() {
    companion object Key : Message.Key<FriendFlashImage>
}
