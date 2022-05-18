/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.announcement

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.announcement.AnnouncementParameters.Companion.DEFAULT
import net.mamoe.mirai.utils.isSameClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * 群公告的附加参数.
 *
 * 可通过 [AnnouncementParametersBuilder] 构建. 默认实例为 [DEFAULT].
 *
 * @since 2.7
 */
@SerialName(AnnouncementParameters.SERIAL_NAME)
@Serializable
public class AnnouncementParameters internal constructor(
    /**
     * 群公告的图片，目前仅支持发送图片，不支持获得图片. 可通过 [Announcements.uploadImage] 上传图片.
     * @see AnnouncementImage
     */
    public val image: AnnouncementImage? = null,
    /** 发送给新成员 */
    public val sendToNewMember: Boolean = false,
    /** 置顶. 可以有多个置顶公告 */
    public val isPinned: Boolean = false,
    /** 显示能够引导群成员修改昵称的窗口 */
    public val showEditCard: Boolean = false,
    /** 使用弹窗 */
    public val showPopup: Boolean = false,
    /** 需要群成员确认 */
    public val requireConfirmation: Boolean = false,
) {
    /**
     * 以该对象作为原型创建一个 [AnnouncementParametersBuilder].
     */
    public fun builder(): AnnouncementParametersBuilder = AnnouncementParametersBuilder().apply {
        val outer = this@AnnouncementParameters
        image(outer.image)
        sendToNewMember(outer.sendToNewMember)
        isPinned(outer.isPinned)
        showEditCard(outer.showEditCard)
        showPopup(outer.showPopup)
        requireConfirmation(outer.requireConfirmation)
    }

    public companion object {
        public const val SERIAL_NAME: String = "AnnouncementParameters"

        /**
         * 默认值的 [AnnouncementParameters] 实例
         */
        @JvmStatic
        @get:JvmName("getDefault")
        public val DEFAULT: AnnouncementParameters = AnnouncementParameters()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnouncementParameters || !isSameClass(this, other)) return false

        if (image != other.image) return false
        if (sendToNewMember != other.sendToNewMember) return false
        if (isPinned != other.isPinned) return false
        if (showEditCard != other.showEditCard) return false
        if (showPopup != other.showPopup) return false
        if (requireConfirmation != other.requireConfirmation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = image?.hashCode() ?: 0
        result = 31 * result + sendToNewMember.hashCode()
        result = 31 * result + isPinned.hashCode()
        result = 31 * result + showEditCard.hashCode()
        result = 31 * result + showPopup.hashCode()
        result = 31 * result + requireConfirmation.hashCode()
        return result
    }

    override fun toString(): String {
        return "AnnouncementParameters(image=$image, sendToNewMember=$sendToNewMember, isPinned=$isPinned, showEditCard=$showEditCard, showPopup=$showPopup, requireConfirmation=$requireConfirmation)"
    }
}