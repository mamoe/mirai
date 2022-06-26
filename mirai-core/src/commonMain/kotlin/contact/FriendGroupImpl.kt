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
import net.mamoe.mirai.internal.contact.info.FriendGroupInfoImpl
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList

private suspend fun refreshFriendGroupList(bot: QQAndroidBot): MutableList<FriendGroup> {
    val friendGroups = mutableListOf<FriendGroup>()

    var count = 0
    var total: Short
    while (true) {
        val data = bot.network.sendAndExpect(
            FriendList.GetFriendGroupList(bot.client, 0, 0, count, 150)
        )

        total = data.totoalGroupCount

        for (jceInfo in data.groupList) {
            friendGroups.add(
                FriendGroupImpl(
                    bot, FriendGroupInfoImpl(
                        jceInfo.groupId.toInt(),
                        jceInfo.groupname,
                        jceInfo.friendCount,
                        jceInfo.onlineFriendCount
                    )
                )
            )
        }

        count += data.groupList.size
        if (count >= total) break
    }
    return friendGroups
}

internal class FriendGroupImpl constructor(
    val bot: QQAndroidBot,
    override val info: FriendGroupInfoImpl
) : FriendGroup {
    override val id: Int by info::groupId

    // todo rename
    override val name: String by info::groupName
    override val friendCount: Int by info::friendCount

    override suspend fun rename(newName: String) {
        bot.network.sendAndExpect(FriendList.SetGroupReqPack.Rename(bot.client, newName, id)).let {
            check(it.isSuccess) {
                "Cannot rename friendGroup(id=$id) to $newName, code=${it.result.toInt()}, errStr=${it.errStr}"
            }
        }
        info.groupName = newName
    }

    override suspend fun moveIn(friend: Friend) {
        bot.network.sendAndExpect(FriendList.MoveGroupMemReqPack(bot.client, friend.id, id)).let {
            check(it.isSuccess) {
                "Cannot move friend to $this, code=${it.result.toInt()}, errStr=${it.errStr}"
            }
        }
    }

    override suspend fun delete() {
        bot.network.sendAndExpect(FriendList.SetGroupReqPack.Delete(bot.client, id)).let {
            check(it.isSuccess) {
                "Cannot delete friendGroup, code=${it.result.toInt()}, errStr=${it.errStr}"
            }
        }
        bot.friendGroups.remove(this)
    }

    override suspend fun new(name: String): FriendGroup {
        val resp = bot.network.sendAndExpect(FriendList.SetGroupReqPack.New(bot.client, name))
        check(resp.isSuccess) {
            "Cannot create friendGroup, code=${resp.result.toInt()}, errStr=${resp.errStr}"
        }
        println("re: " + resp.groupId)
        // for review, 我没找到 getSingleFriendGroupInfo 这种类似的包, 目前是全部刷新一遍
        bot.friendGroups = refreshFriendGroupList(bot)
        return bot.friendGroups.first { it.id == resp.groupId }
    }

    override fun toString(): String {
        return "FriendGroup(id=$id, name=$name)"
    }
}