/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.internal.permission

import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.AbstractPermitteeId.*

internal fun parseFromStringImpl(string: String): AbstractPermitteeId {
    val str = string.trim { it.isWhitespace() }.toLowerCase()
    if (str == "console") return Console
    if (str.isNotEmpty()) {
        when (str[0]) {
            'g' -> {
                val arg = str.substring(1)
                if (arg == "*") return AnyGroup
                else arg.toLongOrNull()?.let(::ExactGroup)?.let { return it }
            }
            'f' -> {
                val arg = str.substring(1)
                if (arg == "*") return AnyFriend
                else arg.toLongOrNull()?.let(::ExactFriend)?.let { return it }
            }
            'u' -> {
                val arg = str.substring(1)
                if (arg == "*") return AnyUser
                else arg.toLongOrNull()?.let(::ExactUser)?.let { return it }
            }
            'c' -> {
                val arg = str.substring(1)
                if (arg == "*") return AnyContact
            }
            'm' -> kotlin.run {
                val arg = str.substring(1)
                if (arg == "*") return AnyMemberFromAnyGroup
                else {
                    val components = arg.split('.')

                    if (components.size == 2) {
                        val groupId = components[0].toLongOrNull() ?: return@run

                        if (components[1] == "*") return AnyMember(groupId)
                        else {
                            val memberId = components[1].toLongOrNull() ?: return@run
                            return ExactMember(groupId, memberId)
                        }
                    }
                }
            }
            't' -> kotlin.run {
                val arg = str.substring(1)
                if (arg == "*") return AnyTempFromAnyGroup
                else {
                    val components = arg.split('.')

                    if (components.size == 2) {
                        val groupId = components[0].toLongOrNull() ?: return@run

                        if (components[1] == "*") return AnyTemp(groupId)
                        else {
                            val memberId = components[1].toLongOrNull() ?: return@run
                            return ExactTemp(groupId, memberId)
                        }
                    }
                }
            }
        }
    }
    error("Cannot deserialize '$str' as AbstractPermissibleIdentifier")
}