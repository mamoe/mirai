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
public interface Guild : Contact,  CoroutineScope {

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
    public val guildCode: Long

    /**
     * 群主.
     *
     * @return 若机器人是群主, 返回 [botAsMember]. 否则返回相应的成员
     */
    public val owner: GuildMember

    /**
     * [Bot] 在群内的 [Member] 实例
     */
    public val botAsMember: GuildMember



    /**
     * 子频道列表
     */
    public val channelNodes : List<Channel>

    /**
     * 频道成员
     */
    public val members : ContactList<GuildMember>

    /**
     * 获取群成员实例. 不存在时返回 `null`.
     *
     * 当 [id] 为 [Bot.id] 时返回 [botAsMember].
     */
    public operator fun get(id: Long): GuildMember?

    /**
     * 获取群成员实例. 不存在时抛出 [kotlin.NoSuchElementException].
     *
     * 当 [id] 为 [Bot.id] 时返回 [botAsMember].
     */
    public fun getOrFail(id: Long): GuildMember =
        get(id) ?: throw NoSuchElementException("member $id not found in guild ${this.id}")


    /**
     * 当本群存在 [Member.id] 为 [id] 的群员时返回 `true`.
     *
     * 当 [id] 为 [Bot.id] 时返回 `true`
     */
    public operator fun contains(id: Long): Boolean

    /**
     * 当 [member] 是本群成员时返回 `true`. 将同时成员 [所属群][Member.group]. 同一个用户在不同群内的 [Member] 对象不相等.
     */
    public operator fun contains(member: GuildMember): Boolean = member in members

    /**
     * 让机器人退出这个频道.
     * @throws IllegalStateException 当机器人为频道主时
     * @return 退出成功时 true; 已经退出时 false
     */
    public suspend fun quit(): Boolean
}