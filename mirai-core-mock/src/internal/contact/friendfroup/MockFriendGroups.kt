/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.friendfroup

import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.utils.ConcurrentLinkedDeque
import net.mamoe.mirai.utils.asImmutable
import kotlin.math.absoluteValue
import kotlin.random.Random

internal class MockFriendGroups(
    private val bot: MockBot,
) : FriendGroups {
    internal val groups = ConcurrentLinkedDeque<MockFriendGroup>()
    private val defaultX = MockFriendGroup(bot, 0, "默认分组")

    override val default: FriendGroup get() = defaultX

    init {
        groups.addLast(defaultX)
    }

    override suspend fun create(name: String): FriendGroup {
        var newId: Int
        do {
            newId = Random.nextInt().absoluteValue
        } while (groups.any { it.id == newId })

        val newG = MockFriendGroup(bot, newId, name)
        groups.addLast(newG)
        return newG
    }

    override fun get(id: Int): FriendGroup? {
        if (id == 0) return defaultX
        return groups.find { it.id == id }
    }

    override fun asCollection(): Collection<FriendGroup> {
        return groups.asImmutable()
    }

    fun findOrDefault(friendGroupId: Int): FriendGroup {
        return get(friendGroupId) ?: defaultX
    }
}