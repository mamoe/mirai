/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.announcement

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 群公告的附加参数.
 *
 * 可通过 [AnnouncementParametersBuilder] 构建.
 *
 * @since 2.7
 */
@SerialName(AnnouncementParameters.SERIAL_NAME)
@Serializable
public class AnnouncementParameters internal constructor(
    /**
     * 群公告的图片，目前仅支持发送图片，不支持获得图片.
     * @see AnnouncementImage
     */
    public val image: AnnouncementImage? = null,

    /**
     * 是否发送给新成员
     */
    public val sendToNewMember: Boolean = false,

    /**
     * 是否置顶，可以有多个置顶公告
     */
    public val isPinned: Boolean = false,

    /**
     * 是否显示能够引导群成员修改昵称的窗口
     */
    public val isShowEditCard: Boolean = false,

    /**
     * 是否使用弹窗
     */
    public val isTip: Boolean = false,

    /**
     * 是否需要群成员确认
     */
    public val needConfirm: Boolean = false,
) {
    /**
     * 以该对象作为原型创建一个 [AnnouncementParametersBuilder].
     */
    public fun builder(): AnnouncementParametersBuilder = AnnouncementParametersBuilder().apply {
        val outer = this@AnnouncementParameters
        this.image = outer.image
        this.sendToNewMember = outer.sendToNewMember
        this.isPinned = outer.isPinned
        this.isShowEditCard = outer.isShowEditCard
        this.isTip = outer.isTip
        this.needConfirm = outer.needConfirm
    }

    public companion object {
        public const val SERIAL_NAME: String = "AnnouncementParameters"

        /**
         * 使用默认值的 [AnnouncementParameters] 实例
         */
        @JvmStatic
        @get:JvmName("getDefault")
        public val DEFAULT: AnnouncementParameters = AnnouncementParameters()
    }
}