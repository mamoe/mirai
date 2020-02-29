/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * 标示这个 API 是低级的 API.
 *
 * 使用低级的 API 无法带来任何安全和便捷保障.
 * 仅在某些使用结构化 API 可能影响性能的情况下使用这些低级 API.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class LowLevelAPI

/**
 * [Bot] 相关协议层低级 API.
 */
@MiraiExperimentalAPI
@Suppress("FunctionName", "unused")
@LowLevelAPI
interface LowLevelBotAPIAccessor {
    /**
     * 撤回一条由机器人发送给好友的消息
     * @param messageId [MessageSource.id]
     */
    @MiraiExperimentalAPI("还未实现")
    @LowLevelAPI
    suspend fun _lowLevelRecallFriendMessage(friendId: Long, messageId: Long, time: Long)

    /**
     * 撤回一条群里的消息. 可以是机器人发送也可以是其他群员发送.
     * @param messageId [MessageSource.id]
     */
    @LowLevelAPI
    suspend fun _lowLevelRecallGroupMessage(groupId: Long, messageId: Long)
}