/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.mock.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.mock.contact.active.MockGroupActive
import net.mamoe.mirai.mock.contact.announcement.MockAnnouncements
import net.mamoe.mirai.mock.userprofile.MockMemberInfoBuilder
import net.mamoe.mirai.utils.cast
import kotlin.random.Random

@JvmBlockingBridge
public interface MockGroup : Group, MockContact, MockMsgSyncSupport {
    /** @see net.mamoe.mirai.IMirai.getUin */
    public val uin: Long
    override val bot: MockBot
    override val members: ContactList<MockNormalMember>
    override val owner: MockNormalMember
    override val botAsMember: MockNormalMember
    override val avatarUrl: String
    override val announcements: MockAnnouncements
    override val active: MockGroupActive

    public interface MockApi : MockContact.MockApi {
        override var avatarUrl: String
    }

    override val mockApi: MockApi

    /**
     * 群荣耀, 可直接修改此属性, 修改此属性不会广播相关事件
     *
     * @see changeHonorMember
     */
    @MockBotDSL
    @Deprecated(
        "use active.changeHonorMember",
        ReplaceWith(".active.changeHonorMember(member, honorType)"),
        level = DeprecationLevel.ERROR
    )
    public val honorMembers: MutableMap<GroupHonorType, MockNormalMember>

    /**
     * 更改拥有群荣耀的群成员.
     *
     * 会自动广播 [MemberHonorChangeEvent.Achieve] 和 [MemberHonorChangeEvent.Lose] 等相关事件.
     *
     * 此外如果 [honorType] 是 [GroupHonorType.TALKATIVE],
     * 会额外广播 [net.mamoe.mirai.event.events.GroupTalkativeChangeEvent].
     *
     * 如果不需要广播事件, 可直接使用 [MockGroupActive.mockSetHonorHistory]
     */
    public fun changeHonorMember(member: MockNormalMember, honorType: GroupHonorType) {
        active.changeHonorMember(member, honorType)
    }

    /**
     * 获取群控制面板
     *
     * 注, 通过本属性获取的控制面板为原始数据存储面板, 修改并不会广播相关事件, 如果需要广播事件,
     * 请使用 [MockGroupControlPane.withActor]
     */
    @MockBotDSL
    public val controlPane: MockGroupControlPane

    /** 添加一位成员, 该操作不会广播任何事件
     * @see MockMemberInfoBuilder
     */
    @MockBotDSL
    public fun appendMember(mockMember: MemberInfo): MockGroup //  chain call

    /**
     * 添加一位成员, 该操作不会广播任何事件
     * @see MockMemberInfoBuilder
     */
    @MockBotDSL
    public fun addMember(mockMember: MemberInfo): MockNormalMember

    /** 添加一位成员, 该操作不会广播任何事件
     */
    @MockBotDSL
    public fun appendMember(uin: Long, nick: String): MockGroup =
        appendMember(MockMemberInfoBuilder.create { uin(uin).nick(nick) })

    /** 添加一位成员, 该操作不会广播任何事件
     * @see MockMemberInfoBuilder
     */
    @MockBotDSL
    public fun addMember(uin: Long, nick: String): MockNormalMember =
        addMember(MockMemberInfoBuilder.create { uin(uin).nick(nick) })


    /**
     * 修改群主, 该操作会广播群转让的相关事件
     */
    @MockBotDSL
    public suspend fun changeOwner(member: NormalMember)

    /**
     * 修改群主, 该操作不会广播任何事件
     */
    @MockBotDSL
    public fun changeOwnerNoEventBroadcast(member: NormalMember)

    /**
     * 创建新的匿名群成员.
     *
     * @param id 该匿名群成员的 id, 可自定义, 建议使用 ASCII 纯文本
     */
    @MockBotDSL
    public fun newAnonymous(nick: String, id: String): MockAnonymousMember

    override fun get(id: Long): MockNormalMember?
    override fun getOrFail(id: Long): MockNormalMember = super.getOrFail(id).cast()

    /**
     * 主动广播有新成员申请加入的事件
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

