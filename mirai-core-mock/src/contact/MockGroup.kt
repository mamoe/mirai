/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.mock.contact.announcement.MockAnnouncements
import net.mamoe.mirai.mock.userprofile.MockMemberInfoBuilder
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.cast
import java.util.function.Consumer
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.random.Random

@JvmBlockingBridge
public interface MockGroup : Group, MockContact, MockMsgSyncSupport {
    /** @see net.mamoe.mirai.IMirai.getUin */
    public var uin: Long
    override val bot: MockBot
    override val members: ContactList<MockNormalMember>
    override val owner: MockNormalMember
    override val botAsMember: MockNormalMember
    override val announcements: MockAnnouncements

    /**
     * 获取群控制面板
     *
     * 注, 通过本属性获取的控制面板为原始数据存储面板, 修改并不会广播相关事件, 如果需要广播事件,
     * 请使用 [MockGroupControlPane.withActor]
     */
    public val controlPane: MockGroupControlPane

    /** 添加一位成员, 该操作不会广播任何事件 */
    @MockBotDSL
    public fun addMember(mockMember: MemberInfo): MockGroup //  chain call

    /** 添加一位成员, 该操作不会广播任何事件 */
    @MockBotDSL
    public fun addMember0(mockMember: MemberInfo): MockNormalMember

    /** 添加一位成员, 该操作不会广播任何事件 */
    @MockBotDSL
    @JavaFriendlyAPI
    @LowPriorityInOverloadResolution
    public fun addMember(id: Long, nick: String, action: Consumer<MockMemberInfoBuilder>): MockGroup {
        return addMember(MockMemberInfoBuilder().uin(id).nick(nick).also { action.accept(it) }.build())
    }

    // Will have event broadcast
    @MockBotDSL
    public suspend fun changeOwner(member: NormalMember)

    @MockBotDSL
    public fun changeOwnerNoEventBroadcast(member: NormalMember)

    @MockBotDSL
    public fun newAnonymous(nick: String, id: String): MockAnonymousMember

    override fun get(id: Long): MockNormalMember?
    override fun getOrFail(id: Long): MockNormalMember = super.getOrFail(id).cast()

    /**
     * 主动广播有新成员加入的事件
     */
    @MockBotDSL
    public suspend fun broadcastNewMemberJoinRequestEvent(
        requester: Long,
        requesterName: String,
        message: String,
        invitor: Long = 0L,
    ): MemberJoinRequestEvent {
        return MemberJoinRequestEvent(
            bot, Random.nextLong(),
            message,
            requester,
            this.id,
            this.name,
            requesterName,
            invitor.takeIf { it != 0L },
        ).broadcast()
    }
}

/** 添加一位成员, 该操作不会广播任何事件 */
@MockBotDSL
public inline fun MockGroup.addMember(id: Long, nick: String, action: MockMemberInfoBuilder.() -> Unit): MockGroup {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return addMember(MockMemberInfoBuilder().uin(id).nick(nick).also(action).build())
}
