/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge
@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.contact.vote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.annotations.Range
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 投票发布时的信息
 * @see Vote
 * @since 2.15
 */
@Serializable
@SerialName("VoteDescription")
public class VoteDescription internal constructor(
    /**
     * 标题
     */
    public val title: String,

    /**
     * 选项列表
     */
    public val options: List<String>,
    /**
     * 群投票的图片，目前仅支持发送图片，不支持获得图片. 可通过 [Votes.uploadImage] 上传图片.
     * @see VoteImage
     */
    public val image: VoteImage? = null,
    /**
     * 匿名
     */
    public val isAnonymous: Boolean = false,
    /**
     * 从发布到结束的时间间隔，单位秒
     * @see duration
     */
    public val durationSeconds: Long = 3 * 24 * 60 * 60,
    /**
     * 从发布到提醒群内还未完成投票的群员的时间间隔，单位秒
     * @see remind
     */
    public val remindSeconds: Long = 3 * 24 * 60 * 60 - 30 * 60,
    /**
     * 用户投票时可选的选项数量, 单选为 `1`, 多选为大于 `1`
     */
    public val availableVotes: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int = 1
) {
    /**
     * 发布投票
     */
    public suspend inline fun publishTo(group: Group): Vote = group.votes.publish(vote = this)

    /**
     * 以该对象作为原型创建一个 [VoteDescriptionBuilder].
     */
    public fun builder(): VoteDescriptionBuilder = VoteDescriptionBuilder(this)
    override fun toString(): String {
        return "VoteDescription(title='$title', options=$options, image=$image, isAnonymous=$isAnonymous, durationSeconds=$durationSeconds, remindSeconds=$remindSeconds, availableVotes=$availableVotes)"
    }


}


/**
 * 从发布到提醒的时间间隔
 * @see VoteDescription.remindSeconds
 * @since 2.15
 */
public val VoteDescription.remind: Duration get() = remindSeconds.seconds

/**
 * 从发布到结束的时间间隔
 * @see VoteDescription.durationSeconds
 * @since 2.15
 */
public val VoteDescription.duration: Duration get() = durationSeconds.seconds
