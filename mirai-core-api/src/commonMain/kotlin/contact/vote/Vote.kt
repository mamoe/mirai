/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact.vote

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.annotations.Range


/**
 * 表示从 [Votes.get] 等途径在线获取的, 已经存在于服务器的投票.
 *
 * [Vote] 拥有唯一识别属性 [fid] 代表其存在于服务器中的 ID. 可进行 [删除][delete]
 *
 * 此对象为某一时刻的统计信息快照, 不会随投票的更新而更新. 若要更新, 使用 [refresh].
 *
 * @since 2.15
 */
@NotStableForInheritance
public interface Vote {
    /**
     * 该投票发布时的信息, 包含标题, 附加参数等.
     */
    public val description: VoteDescription

    /**
     * 投票所属群
     */
    public val group: Group

    /**
     * 投票发送者 [NormalMember.id]
     */
    public val publisherId: Long

    /**
     * 投票发送者. 当该成员已经离开群后为 `null`
     */
    public val publisher: NormalMember?

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
     *
     * 可以通过 [refresh] 同步内容
     *
     * @see options
     * @see refresh
     */
    public val options: List<VoteOption>

    /**
     * 选项的投票记录
     *
     * 可以通过 [refresh] 同步内容
     *
     * @see options
     * @see refresh
     */
    public val records: List<VoteRecord>

    /**
     * 删除这个投票. 需要管理员权限. 使用 [Votes.delete] 与此方法效果相同.
     *
     * @return 成功返回 `true`, 群投票已被删除时返回 `false`
     * @throws IllegalStateException 当协议异常时抛出
     * @throws PermissionDeniedException 当 [bot] 无管理员权限时抛出
     * @see Votes.delete
     */
    public suspend fun delete(): Boolean = group.votes.delete(fid = fid)

    /**
     * 更新此投票的统计信息, 如 [options], [records].
     *
     * @return 操作成功时返回 `true`, 当群投票已经被删除时返回 `false`.
     * @throws IllegalStateException 当协议异常时抛出
     */
    public suspend fun refresh(): Boolean

    /**
     * 获取更新的投票统计信息, 并使用该信息创建新的 [Vote] 实例. 不会更新当前 [Vote] 实例.
     */
    public suspend fun refreshed(): Vote?
}

/**
 * 投票所在群所属的 [Bot], 即 `group.bot`.
 * @since 2.15
 */
public inline val Vote.bot: Bot get() = group.bot

/**
 * 一次投票记录
 * @since 2.15
 */
@NotStableForInheritance
public interface VoteRecord {
    /**
     * 投票者 [NormalMember.id]
     */
    public val voterId: Long

    /**
     * 投票者. 若该成员在本函数第一次调用时已经离开群, 返回 `null`. 若该成员在这之后离开群, 本属性不会变化.
     */
    public val voter: NormalMember?

    /**
     * 选择的选项
     */
    public val selectedOptions: List<VoteOption>

    /**
     * 时间戳
     */
    public val time: Long
}

/**
 * 表示一次投票中的一个选项.
 * @since 2.15
 */
@NotStableForInheritance
public interface VoteOption {
    /**
     * 选项的序号.
     */
    public val index: Int

    /**
     * 选项的名称.
     */
    public val name: String

    /**
     * 总共被投票数.
     */
    public val totalVotes: @Range(from = 0L, to = Int.MAX_VALUE.toLong()) Int

    /**
     * 投了这个选项的群成员列表.
     */
    public val voterIds: List<Long>

    /**
     * 投了这个选项的群成员列表.
     *
     * 注意若成员在本函数第一次调用时已经离开群, 则此列表中不会包含该群员. 若该成员在这之后离开群, 本属性不会变化.
     */
    public val voters: List<NormalMember>
}