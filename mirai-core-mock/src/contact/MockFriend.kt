/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendAddEvent
import net.mamoe.mirai.event.events.FriendInputStatusChangedEvent
import net.mamoe.mirai.mock.MockBotDSL
import kotlin.random.Random

@JvmBlockingBridge
public interface MockFriend : Friend, MockContact, MockUser, MockMsgSyncSupport {
    public interface MockApi {
        public val contact: MockFriend
        public var nick: String
        public var remark: String
        public var avatarUrl: String
    }

    /**
     * 获取直接修改字段内容的 API, 通过该 API 修改的值都不会触发广播
     */
    public val mockApi: MockApi

    /**
     * 修改 nick 同时广播相关事件
     */
    override var nick: String

    /**
     * 修改 remark 同时广播相关事件
     */
    override var remark: String

    /**
     * 广播好友添加事件
     */
    @MockBotDSL
    public suspend fun broadcastFriendAddEvent(): FriendAddEvent {
        return FriendAddEvent(this).broadcast()
    }

    /**
     * 广播好友邀请 [bot] 加入一个群聊的事件
     */
    @MockBotDSL
    public suspend fun broadcastInviteBotJoinGroupRequestEvent(
        groupId: Long, groupName: String,
    ): BotInvitedJoinGroupRequestEvent {
        return BotInvitedJoinGroupRequestEvent(
            bot,
            Random.nextLong(),
            id,
            groupId,
            groupName,
            nick
        ).broadcast()
    }

    /**
     * 广播好友主动删除 [bot] 好友的事件
     *
     * 即使该函数体实现为 [delete], 也请使用该方法广播 **bot 被好友删除**，
     * 以确保不会受到未来的事件架构变更带来的影响
     */
    @MockBotDSL
    public suspend fun broadcastFriendDelete() {
        delete()
    }

    /**
     * 广播好友输入状态改变事件
     */
    @MockBotDSL
    public suspend fun broadcastFriendInputStateChange(inputting: Boolean) {
        FriendInputStatusChangedEvent(this, inputting).broadcast()
    }
}