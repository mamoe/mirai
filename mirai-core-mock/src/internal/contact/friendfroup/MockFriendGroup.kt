/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.friendfroup

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockFriend
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.cast

internal class MockFriendGroup(
    private val bot: MockBot,
    override val id: Int,
    override var name: String,
) : FriendGroup {
    override val friends: Collection<Friend> = object : AbstractCollection<Friend>() {

        private val seq = sequence<Friend> {
            bot.friends.forEach { mf ->
                if (mf.mockApi.friendGroupId == id) {
                    yield(mf)
                }
            }
        }

        override fun isEmpty(): Boolean {
            return bot.friends.none { it.mockApi.friendGroupId == id }
        }

        override fun contains(element: Friend): Boolean {
            if (element !is MockFriend) return false
            if (element.bot !== bot) return false
            return element.mockApi.friendGroupId == id
        }

        override val size: Int
            get() = bot.friends.count { it.mockApi.friendGroupId == id }

        override fun iterator(): Iterator<Friend> {
            return seq.iterator()
        }

    }

    override suspend fun renameTo(newName: String): Boolean {
        name = newName
        return true
    }

    override suspend fun moveIn(friend: Friend): Boolean {
        val api = friend.mock().mockApi
        if (api.friendGroupId == id) return false

        api.friendGroupId = id
        return true
    }

    override suspend fun delete(): Boolean {
        if (id == 0) return false
        if (bot.friendGroups.cast<MockFriendGroups>().groups.remove(this)) {
            friends.forEach { it.mock().mockApi.friendGroupId = 0 }
            return true
        }
        return false
    }

    override val count: Int get() = friends.size
}