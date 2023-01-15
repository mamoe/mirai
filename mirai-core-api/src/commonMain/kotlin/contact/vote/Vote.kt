/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.vote

import net.mamoe.mirai.contact.Group


/**
 * 表示一个群投票
 * @since 2.14
 */
public sealed interface Vote {
    /**
     * 标题
     */
    public val title: String

    /**
     * 选项
     */
    public val options: List<String>

    /**
     * 附加参数. 可以通过 [VoteParametersBuilder] 构建获得.
     * @see VoteParameters
     * @see VoteParametersBuilder
     */
    public val parameters: VoteParameters

    /**
     * 在该群发布群投票并获得 [OnlineVote]. 发布公告后群内将会出现 "有新投票" 系统提示.
     * @throws IllegalStateException 当协议异常时抛出
     * @see Votes.publish
     */
    public suspend fun publishTo(group: Group): OnlineVote = TODO()

}