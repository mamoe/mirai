/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.data.FriendGroup
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.info.FriendGroupInfo
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList

internal class FriendGroupImpl constructor(
    val bot: QQAndroidBot,
    val info: FriendGroupInfo
) : FriendGroup {
    override val id: Int by info::groupId

    override val name: String by info::groupName
    override val count: Int by info::friendCount


    override suspend fun renameTo(newName: String): Boolean {
        bot.network.sendAndExpect(FriendList.SetGroupReqPack.Rename(bot.client, newName, id)).let {
            if (it.result.toInt() == 1) {
                return false
            }
            check(it.isSuccess) {
                "Cannot rename friendGroup(id=$id) to $newName, code=${it.result.toInt()}, errStr=${it.errStr}"
            }
        }
        info.groupName = newName
        return true
    }

    override suspend fun moveIn(friend: Friend): Boolean {
        bot.network.sendAndExpect(FriendList.MoveGroupMemReqPack(bot.client, friend.id, id)).let {
            check(it.isSuccess) {
                "Cannot move friend to $this, code=${it.result.toInt()}, errStr=${it.errStr}"
            }
        }
        // 因为 MoveGroupMemReqPack 协议在测试里如果移动到不存在的分组，他会自动移动好友到 id = 0 的默认好友分组然后返回 result = 0
        val id = friend.queryProfile().friendGroupId
        if (id != this.id && id == 0) return false
        return true
    }

    override suspend fun delete(): Boolean = bot.friendGroups.delete(this)

    override fun toString(): String {
        return "FriendGroup(id=$id, name=$name)"
    }
}