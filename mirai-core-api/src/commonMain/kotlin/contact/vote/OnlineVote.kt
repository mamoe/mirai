/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "NOTHING_TO_INLINE")
@file:JvmBlockingBridge

package net.mamoe.mirai.contact.vote

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.utils.NotStableForInheritance


/**
 * 表示从 [Votes.get] 等途径在线获取的, 已经存在于服务器的投票.
 *
 * [OnlineVote] 拥有唯一识别属性 [fid] 代表其存在于服务器中的 ID. 可进行 [删除][delete]
 *
 * 可在 [Vote] 获取更多信息.
 *
 * @since 2.14
 */
@NotStableForInheritance
public interface OnlineVote : Vote {
    /**
     * 投票所属群
     */
    public val group: Group

    /**
     * 投票发送者 [NormalMember.id]
     */
    public val senderId: Long

    /**
     * 投票发送者. 当该成员已经离开群后为 `null`
     */
    public val sender: NormalMember?

    /**
     * 唯一识别属性
     */
    public val fid: String

    /**
     * 投票发出的时间，为 EpochSecond (自 1970-01-01T00：00：00Z 的秒数)
     *
     * @see java.time.Instant.ofEpochSecond
     */
    public val publicationTime: Long

    /**
     * 投票截至的时间，为 EpochSecond (自 1970-01-01T00：00：00Z 的秒数)
     *
     * @see java.time.Instant.ofEpochSecond
     */
    public val endTime: Long

    /**
     * 投票详情页 URL
     */
    public val url: String get() = "https://client.qun.qq.com/qqweb/m/qun/vote/detail.html?fid=${fid}&groupuin=${group.id}"

    /**
     * 各个选项的投票数
     * @see options
     */
    public val select: List<Int>

    /**
     * 删除这个投票. 需要管理员权限. 使用 [Votes.delete] 与此方法效果相同.
     *
     * @return 成功返回 `true`, 群投票已被删除时返回 `false`
     * @throws IllegalStateException 当协议异常时抛出
     * @see Votes.delete
     */
    public suspend fun delete(): Boolean = group.votes.delete(fid = fid)

    /**
     * 获取 选项的投票记录
     *
     * @return 投票记录列表
     *
     * @throws IllegalStateException 当协议异常时抛出
     */
    public suspend fun records(): List<OnlineVoteRecord> = group.votes.get(fid = fid)?.records.orEmpty()
}

/**
 * 投票所在群所属的 [Bot], 即 `group.bot`.
 * @since 2.14
 */
public inline val OnlineVote.bot: Bot get() = group.bot

/**
 * 一次投票记录
 */
@NotStableForInheritance
public interface OnlineVoteRecord {
    /**
     * 原投票
     */
    public val vote: Vote

    /**
     * 投票者 [NormalMember.id]
     */
    public val voterId: Long

    /**
     * 投票者. 当该成员已经离开群后为 `null`
     */
    public val voter: NormalMember?

    /**
     * 选择的选项
     */
    public val options: List<Int>

    /**
     * 时间戳
     */
    public val time: Long
}

@NotStableForInheritance
public data class OnlineVoteStatus(
    val vote: OnlineVote,
    val records: List<OnlineVoteRecord>
)
