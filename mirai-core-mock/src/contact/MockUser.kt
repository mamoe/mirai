/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.mock.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockActions
import net.mamoe.mirai.mock.MockActions.mockFireRecalled
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.mock.utils.broadcastMockEvents
import net.mamoe.mirai.utils.JavaFriendlyAPI
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.internal.LowPriorityInOverloadResolution

@JvmBlockingBridge
public interface MockUser : MockContact, MockUserOrBot, User {
    /**
     * 令 [MockUserOrBot] 撤回一条消息
     *
     * @see [mockFireRecalled]
     */
    @MockBotDSL
    public suspend fun recallMessage(message: MessageChain) {
        broadcastMockEvents {
            message.recalledBy(this@MockUser)
        }
    }

    /**
     * 令 [MockUserOrBot] 撤回一条消息
     *
     * @see [mockFireRecalled]
     */
    @MockBotDSL
    public suspend fun recallMessage(message: MessageSource) {
        broadcastMockEvents {
            message.recalledBy(this@MockUser)
        }
    }

    /**
     * 令 [MockUserOrBot] 撤回一条消息
     *
     * @see [mockFireRecalled]
     */
    @MockBotDSL
    public suspend fun recallMessage(message: MessageReceipt<*>) {
        mockFireRecalled(message, this)
    }


    /**
     * 令 [MockContact] 发出一条信息, 并广播相关的消息事件 (如 [GroupMessageEvent])
     *
     * @return 返回 [MockContact] 发出的消息 (包含 [MessageSource]),
     *         可用于测试消息发出后马上撤回 `says().recall()`
     *
     * @see [MockActions.mockFireRecalled]
     * @see [MockUser.recallMessage]
     */
    @MockBotDSL
    public suspend fun says(message: MessageChain): MessageChain


    @MockBotDSL
    public suspend fun says(message: Message): MessageChain {
        return says(message.toMessageChain())
    }

    @MockBotDSL
    public suspend fun says(message: String): MessageChain {
        return says(PlainText(message))
    }

    @JavaFriendlyAPI
    @LowPriorityInOverloadResolution
    public suspend fun says(message: Consumer<MessageChainBuilder>): MessageChain {
        return says(buildMessageChain { message.accept(this) })
    }


    @JavaFriendlyAPI
    @LowPriorityInOverloadResolution
    public suspend fun says(message: Supplier<Message>): MessageChain {
        return says(message.get())
    }

    public suspend fun says(message: suspend MessageChainBuilder.() -> Unit): MessageChain {
        return says(buildMessageChain { message(this) })
    }
}