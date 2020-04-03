@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 闪照
 *
 * @see Image.flash
 */
@SinceMirai("")
sealed class FlashImage : MessageContent {
    companion object Key : Message.Key<FlashImage> {
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
    }

    /**
     * 闪照的图片, 不同于普通的图片.
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
}

@JvmSynthetic
inline fun Image.flash(): FlashImage = FlashImage(this)

@JvmSynthetic
inline fun GroupImage.flash(): GroupFlashImage = FlashImage(this) as GroupFlashImage

@JvmSynthetic
inline fun FriendImage.flash(): FriendFlashImage = FlashImage(this) as FriendFlashImage

/**
 * @see FlashImage.invoke
 */
class GroupFlashImage @MiraiInternalAPI constructor(override val image: GroupImage) : FlashImage() {
    companion object Key : Message.Key<FlashImage>
}

/**
 * @see FlashImage.invoke
 */
class FriendFlashImage @MiraiInternalAPI constructor(override val image: FriendImage) : FlashImage() {
    companion object Key : Message.Key<FlashImage>
}
