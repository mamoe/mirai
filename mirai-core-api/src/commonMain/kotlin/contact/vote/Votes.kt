/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact.vote

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.Streamable

/**
 * 表示一个群的投票列表 (管理器).
 *
 * ## 获取群投票
 *
 * ### 获取 [Votes] 实例
 *
 * 只可以通过 [Group.votes] 获取一个群的投票列表, 即 [Votes] 实例.
 *
 * ### 获取投票列表
 *
 * 通过 [asFlow] 或 `asStream` 可以获取到*惰性*流, 在从流中收集数据时才会请求服务器获取数据. 通常建议在 Kotlin 使用协程的 [asFlow], 在 Java 使用 `asStream`.
 *
 * 若要获取全部投票列表, 可使用 [toList].
 *
 * ## 发布群公告
 *
 * 查看 [Vote]
 *
 * @since 2.15
 */
@NotStableForInheritance
public interface Votes : Streamable<Vote> {

    /**
     * 在该群发布群投票并获得 [Vote]. 发布投票后群内将会出现 "有新投票" 系统提示.
     * @throws IllegalStateException 当协议异常时抛出
     * @see Vote.publishTo
     */
    public suspend fun publish(vote: Vote): OnlineVote

    /**
     * 上传资源作为群投票图片. 返回值可用于 [VoteParameters.image].
     *
     * **注意**: 需要由调用方[关闭][ExternalResource.close] [resource].
     * @throws IllegalStateException 当协议异常时抛出
     */
    public suspend fun uploadImage(resource: ExternalResource): VoteImage

    /**
     * 删除一个群投票. 使用 [OnlineVote.delete] 与此方法效果相同.
     *
     * @param fid 公告的 [OnlineVote.fid]
     * @return 成功返回 `true`, 群投票不存在时返回 `false`
     *
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineVote.delete
     */
    public suspend fun delete(fid: String): Boolean

    /**
     * 获取一个群投票.
     * @param fid 公告的 [OnlineVote.fid]
     * @return 返回 `null` 表示不存在该 [fid] 的群投票
     * @throws IllegalStateException 当协议异常时抛出
     */
    public suspend fun get(fid: String): OnlineVote?

    /**
     * 更新群投票. 使用 [OnlineVote.update] 与此方法效果相同.
     *
     * @param vote 在线公告
     * @return 成功返回 `true`, 群投票不存在时返回 `false`
     *
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineVote.update
     */
    public suspend fun update(vote: OnlineVote)
}