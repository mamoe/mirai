/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.contact.vote.VoteParameters.Companion.DEFAULT
import net.mamoe.mirai.utils.annotations.Range
import net.mamoe.mirai.utils.isSameClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 群投票的附加参数.
 *
 * 可通过 [VoteParametersBuilder] 构建. 默认实例为 [DEFAULT].
 *
 * @since 2.15
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
    public val isAnonymous: Boolean = false,
    /**
     * 从发布到结束的时间间隔，单位秒
     * @see duration
     */
    public val durationSeconds: Long = 3 * 24 * 60 * 60,
    /**
     * 从发布到提醒的时间间隔，单位秒
     * @see remind
     */
    public val remindSeconds: Long = 3 * 24 * 60 * 60 - 30 * 60,
    /**
     * 用户投票时可选的选项数量, 单选为 `1`, 多选为大于 `1`
     */
    public val availableVotes: @Range(from = 1, to = Long.MAX_VALUE) Int = 1
) {

    /**
     * 以该对象作为原型创建一个 [VoteParametersBuilder].
     */
    public fun builder(): VoteParametersBuilder = VoteParametersBuilder().apply {
        val outer = this@VoteParameters
        image(outer.image)
        anonymous(outer.isAnonymous)
        end(outer.durationSeconds)
        remind(outer.remindSeconds)
        capacity(outer.availableVotes)
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
        if (isAnonymous != other.isAnonymous) return false
        if (durationSeconds != other.durationSeconds) return false
        if (remindSeconds != other.remindSeconds) return false
        if (availableVotes != other.availableVotes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = image?.hashCode() ?: 0
        result = 31 * result + isAnonymous.hashCode()
        result = 31 * result + durationSeconds.hashCode()
        result = 31 * result + remindSeconds.hashCode()
        result = 31 * result + availableVotes.hashCode()
        return result
    }

    override fun toString(): String {
        return "VoteParameters(image=$image, isAnonymous=$isAnonymous, endSeconds=$durationSeconds, remindSeconds=$remindSeconds, availableVotes=$availableVotes)"
    }
}

/**
 * 从发布到提醒的时间间隔
 * @see VoteParameters.remindSeconds
 */
public val VoteParameters.remind: Duration get() = remindSeconds.seconds

/**
 * 从发布到结束的时间间隔
 * @see VoteParameters.durationSeconds
 */
public val VoteParameters.duration: Duration get() = durationSeconds.seconds