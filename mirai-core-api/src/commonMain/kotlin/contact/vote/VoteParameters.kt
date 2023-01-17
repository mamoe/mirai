/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.vote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.announcement.AnnouncementParameters.Companion.DEFAULT
import net.mamoe.mirai.utils.isSameClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * 群投票的附加参数.
 *
 * 可通过 [VoteParametersBuilder] 构建. 默认实例为 [DEFAULT].
 *
 * @since 2.14
 */
@SerialName(VoteParameters.SERIAL_NAME)
@Serializable
public class VoteParameters internal constructor(
    /**
     * 群投票的图片，目前仅支持发送图片，不支持获得图片. 可通过 [Votes.uploadImage] 上传图片.
     * @see VoteImage
     */
    public val image: VoteImage? = null,
    /** 匿名 */
    public val anonymous: Boolean = false,
    /** 结束时间间隔，单位秒 */
    public val end: Long = 3 * 24 * 60 * 60,
    /** 提醒时间间隔，单位秒 */
    public val remind: Long = 3 * 24 * 60 * 60 - 30 * 60,
    /** 可选选项数量 */
    public val capacity: Int = 1
) {
    /**
     * 以该对象作为原型创建一个 [VoteParametersBuilder].
     */
    public fun builder(): VoteParametersBuilder = VoteParametersBuilder().apply {
        val outer = this@VoteParameters
        image(outer.image)
        anonymous(outer.anonymous)
        end(outer.end)
        remind(outer.remind)
        capacity(outer.capacity)
    }

    public companion object {
        public const val SERIAL_NAME: String = "VoteParameters"

        /**
         * 默认值的 [VoteParameters] 实例
         */
        @JvmStatic
        @get:JvmName("getDefault")
        public val DEFAULT: VoteParameters = VoteParameters()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VoteParameters || !isSameClass(this, other)) return false

        if (image != other.image) return false
        if (anonymous != other.anonymous) return false
        if (end != other.end) return false
        if (remind != other.remind) return false
        if (capacity != other.capacity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = image?.hashCode() ?: 0
        result = 31 * result + anonymous.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + remind.hashCode()
        result = 31 * result + capacity.hashCode()
        return result
    }

    override fun toString(): String {
        return "VoteParameters(image=$image, anonymous=$anonymous, end=$end, remind=$remind, capacity=$capacity)"
    }
}