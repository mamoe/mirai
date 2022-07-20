/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.data.FriendGroup
import net.mamoe.mirai.data.FriendGroups
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.info.FriendGroupInfo
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.utils.asImmutable
import java.util.concurrent.ConcurrentLinkedDeque

internal class FriendGroupsImpl(
    val bot: QQAndroidBot
) : Iterable<FriendGroup>, FriendGroups {
    val friendGroups = ConcurrentLinkedDeque<FriendGroup>()

    override suspend fun create(name: String): FriendGroup {
        val resp = bot.network.sendAndExpect(FriendList.SetGroupReqPack.New(bot.client, name))
        check(resp.isSuccess) {
            "Cannot create friendGroup, code=${resp.result.toInt()}, errStr=${resp.errStr}"
        }
        return FriendGroupImpl(bot, FriendGroupInfo(resp.groupId, name, 0, 0)).apply { friendGroups.add(this) }
    }

    override fun get(id: Int): FriendGroup? = friendGroups.firstOrNull { it.id == id }
    override fun iterator(): Iterator<FriendGroup> = friendGroups.asImmutable().iterator()
}