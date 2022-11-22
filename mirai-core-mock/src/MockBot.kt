/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotAvatarChangedEvent
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.OnlineAudio
import net.mamoe.mirai.mock.contact.*
import net.mamoe.mirai.mock.database.MessageDatabase
import net.mamoe.mirai.mock.resserver.TmpResourceServer
import net.mamoe.mirai.mock.userprofile.UserProfileService
import net.mamoe.mirai.mock.utils.AvatarGenerator
import net.mamoe.mirai.mock.utils.NameGenerator
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.cast
import kotlin.random.Random

/**
 * 一个虚拟的机器人对象. 继承于 [Bot]
 *
 * @see MockBotFactory 构造 [MockBot] 的工厂, [MockBot] 的唯一构造方式
 */
@Suppress("unused")
@JvmBlockingBridge
public interface MockBot : Bot, MockContactOrBot, MockUserOrBot {
    override val bot: MockBot get() = this

    /**
     * bot 昵称, 访问此字段时与 [nick] 一致
     * 修改此字段时不会广播事件
     */
    @MockBotDSL
    public var nickNoEvent: String

    /**
     * bot 昵称
     *
     * 修改此字段时会广播事件
     */
    override var nick: String

    /**
     * Bot 头像, 可自定义, 修改时会广播 [BotAvatarChangedEvent]
     */
    @set:MockBotDSL
    override var avatarUrl: String

    /// Contact API override
    override fun getFriend(id: Long): MockFriend? = super.getFriend(id)?.cast()

    override fun getFriendOrFail(id: Long): MockFriend = super.getFriendOrFail(id).cast()

    override fun getGroup(id: Long): MockGroup? = super.getGroup(id)?.cast()

    override fun getGroupOrFail(id: Long): MockGroup = super.getGroupOrFail(id).cast()

    override fun getStranger(id: Long): MockStranger? = super.getStranger(id)?.cast()

    override fun getStrangerOrFail(id: Long): MockStranger = super.getStrangerOrFail(id).cast()

    override val groups: ContactList<MockGroup>
    override val friends: ContactList<MockFriend>
    override val strangers: ContactList<MockStranger>
    override val otherClients: ContactList<MockOtherClient>
    override val asFriend: MockFriend
    override val asStranger: MockStranger

    /// All mock api will not broadcast event

    public val nameGenerator: NameGenerator
    public val tmpResourceServer: TmpResourceServer
    public val msgDatabase: MessageDatabase
    public val userProfileService: UserProfileService

    /** @since 2.14.0 */
    public val avatarGenerator: AvatarGenerator

    /// Mock Contact API

    @MockBotDSL
    public fun addGroup(id: Long, name: String): MockGroup

    @MockBotDSL
    public fun addGroup(id: Long, uin: Long, name: String): MockGroup

    @MockBotDSL
    public fun addFriend(id: Long, name: String): MockFriend

    @MockBotDSL
    public fun addStranger(id: Long, name: String): MockStranger

    /**
     * 将 [resource] 上传到 [临时资源服务器][tmpResourceServer],
     * 并返回一个 [OnlineAudio] 对象, 可用于测试语音接收
     *
     * @see MockUser.says
     */
    @MockBotDSL
    public suspend fun uploadOnlineAudio(resource: ExternalResource): OnlineAudio

    /**
     * 将 [resource] 上传到 [临时资源服务器][tmpResourceServer]
     * 并返回一个 [Image] 对象, 可用于测试图片接收
     *
     * @see MockUser.says
     */
    @MockBotDSL
    public suspend fun uploadMockImage(resource: ExternalResource): Image

    /**
     * 广播 [Bot] 掉线事件
     */
    @MockBotDSL
    public suspend fun broadcastOfflineEvent() {
        BotOfflineEvent.Dropped(this, java.net.SocketException("socket closed")).broadcast()
    }

    /**
     * 广播 [Bot] 头像更新事件
     */
    @MockBotDSL
    public suspend fun broadcastAvatarChangeEvent() {
        BotAvatarChangedEvent(this).broadcast()
    }

    /**
     * 广播新好友添加事件
     *
     * @see NewFriendRequestEvent
     */
    @MockBotDSL
    public suspend fun broadcastNewFriendRequestEvent(
        requester: Long,
        requesterNick: String,
        fromGroup: Long,
        message: String
    ): NewFriendRequestEvent {
        return NewFriendRequestEvent(
            this,
            eventId = Random.nextLong(),
            fromId = requester,
            fromGroupId = fromGroup,
            message = message,
            fromNick = requesterNick
        ).broadcast()
    }
}
