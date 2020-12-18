/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import net.mamoe.mirai.utils.PlannedRemoval

// internal 定义可以通过全名引用, 用户就能看到 ReplaceWith. 新用户则看不到这些旧类型的 import.

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("FriendMessageEvent", "net.mamoe.mirai.event.events.FriendMessageEvent"),
    level = DeprecationLevel.ERROR
)
private typealias FriendMessageEvent = net.mamoe.mirai.event.events.FriendMessageEvent

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("GroupAwareMessageEvent", "net.mamoe.mirai.event.events.GroupAwareMessageEvent"),
    level = DeprecationLevel.ERROR
)
private typealias GroupAwareMessageEvent = net.mamoe.mirai.event.events.GroupAwareMessageEvent

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("GroupMessageEvent", "net.mamoe.mirai.event.events.GroupMessageEvent"),
    level = DeprecationLevel.ERROR
)
private typealias GroupMessageEvent = net.mamoe.mirai.event.events.GroupMessageEvent

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("MessageEvent", "net.mamoe.mirai.event.events.MessageEvent"),
    level = DeprecationLevel.ERROR
)
private typealias MessageEvent = net.mamoe.mirai.event.events.MessageEvent

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("TempMessageEvent", "net.mamoe.mirai.event.events.TempMessageEvent"),
    level = DeprecationLevel.ERROR
)
private typealias TempMessageEvent = net.mamoe.mirai.event.events.TempMessageEvent

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("UserMessageEvent", "net.mamoe.mirai.event.events.UserMessageEvent"),
    level = DeprecationLevel.ERROR
)
private typealias UserMessageEvent = net.mamoe.mirai.event.events.UserMessageEvent

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("MessageEventExtensions", "net.mamoe.mirai.event.events.MessageEventExtensions"),
    level = DeprecationLevel.ERROR
)
private typealias MessageEventExtensions<TSender, TSubject> = net.mamoe.mirai.event.events.MessageEventExtensions<TSender, TSubject>

@PlannedRemoval("2.0-M2")
@Deprecated(
    "Replace with new package.",
    ReplaceWith("MessageEventPlatformExtensions", "net.mamoe.mirai.event.events.MessageEventPlatformExtensions"),
    level = DeprecationLevel.ERROR
)
private typealias MessageEventPlatformExtensions<TSender, TSubject> = net.mamoe.mirai.event.events.MessageEventPlatformExtensions<TSender, TSubject>