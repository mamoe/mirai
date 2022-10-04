/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.NotStableForInheritance

@NotStableForInheritance
public interface Guild : Contact, CoroutineScope {

    /**
     * 频道名称.
     */
    public val name: String

    /**
     * 用户看不到的频道号码.
     */
    public override val id: Long

    /**
     * guildCode
     */
//    public val guildCode: Long

    /**
     * 群主.
     *
     * @return 若机器人是频道主, 返回 [botAsMember]. 否则返回相应的成员
     */
    public val owner: GuildMember

    /**
     * [Bot] 在频道内的 [Member] 实例
     */
    public val botAsMember: GuildMember


    /**
     * 子频道列表
     */
    public val channelNodes: List<Channel>

    /**
     * 频道成员
     */
    public val members: ContactList<GuildMember>

    /**
     * 获取频道成员实例. 不存在时返回 `null`.
     *
     * 当 [tinyId] 为 [Bot.tinyId] 时返回 [botAsMember].
     */
    public operator fun get(tinyId: Long): GuildMember?

    /**
     * 获取频道成员实例. 不存在时抛出 [kotlin.NoSuchElementException].
     *
     * 当 [tinyId] 为 [Bot.tinyId] 时返回 [botAsMember].
     */
    public fun getOrFail(tinyId: Long): GuildMember =
        get(id) ?: throw NoSuchElementException("member $tinyId not found in guild ${this.id}")


    /**
     * 当本频道存在 [GuildMember.id] 为 [tinyId] 的群员时返回 `true`.
     *
     * 当 [tinyId] 为 [Bot.tinyId] 时返回 `true`
     */
    public operator fun contains(tinyId: Long): Boolean

    /**
     * 当 [member] 是本频道成员时返回 `true`. 将同时成员 [所属频道][GuildMember.guild]. 同一个用户在不同频道内的 [GuildMember] 对象不相等.
     */
    public operator fun contains(member: GuildMember): Boolean = member in members

    /**
     * 让机器人退出这个频道.
     * @throws IllegalStateException 当机器人为频道主时
     * @return 退出成功时 true; 已经退出时 false
     */
    public suspend fun quit(): Boolean
}