/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.japt.internal

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.japt.*

inline fun Group.blocking(): BlockingGroup =
    BlockingContacts.createBlocking(this)

inline fun QQ.blocking(): BlockingQQ = BlockingContacts.createBlocking(this)
inline fun Member.blocking(): BlockingMember =
    BlockingContacts.createBlocking(this)

inline fun Bot.blocking(): BlockingBot = BlockingContacts.createBlocking(this)