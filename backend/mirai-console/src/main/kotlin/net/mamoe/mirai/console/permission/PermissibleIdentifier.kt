/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.permission

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 */
@ExperimentalPermission("Classname is subject to change")
public interface PermissibleIdentifier {
    public val parents: Array<out PermissibleIdentifier>

    public companion object {
        @ExperimentalPermission
        public fun PermissibleIdentifier.grantedWith(with: PermissibleIdentifier): Boolean {
            return allParentsWithSelf().any { it == with }
        }

        private fun PermissibleIdentifier.allParentsWithSelf(): Sequence<PermissibleIdentifier> {
            return sequence {
                yield(this@allParentsWithSelf)
                yieldAll(parents.asSequence())
            }
        }
    }
}

@Serializable(with = AbstractPermissibleIdentifier.AsStringSerializer::class)
@ExperimentalPermission
public sealed class AbstractPermissibleIdentifier(
    public final override vararg val parents: PermissibleIdentifier
) : PermissibleIdentifier {
    public companion object {
        @JvmStatic
        public fun parseFromString(string: String): AbstractPermissibleIdentifier {
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
    }

    @ConsoleExperimentalApi
    public object AsStringSerializer : KSerializer<AbstractPermissibleIdentifier> by String.serializer().map(
        serializer = { it.toString() },

        deserializer = d@{ str -> parseFromString(str) }
    )

    public object AnyGroup : AbstractPermissibleIdentifier(AnyContact) {
        override fun toString(): String = "g*"
    }

    public data class ExactGroup(public val groupId: Long) : AbstractPermissibleIdentifier(AnyGroup) {
        override fun toString(): String = "g$groupId"
    }

    public data class AnyMember(public val groupId: Long) : AbstractPermissibleIdentifier(AnyMemberFromAnyGroup) {
        override fun toString(): String = "m$groupId.*"
    }

    public object AnyMemberFromAnyGroup : AbstractPermissibleIdentifier(AnyUser) {
        override fun toString(): String = "m*"
    }

    public object AnyTempFromAnyGroup : AbstractPermissibleIdentifier(AnyUser) {
        override fun toString(): String = "t*"
    }

    public data class ExactMember(
        public val groupId: Long,
        public val memberId: Long
    ) : AbstractPermissibleIdentifier(AnyMember(groupId), ExactUser(memberId)) {
        override fun toString(): String = "m$groupId.$memberId"
    }

    public object AnyFriend : AbstractPermissibleIdentifier(AnyUser) {
        override fun toString(): String = "f*"
    }

    public data class ExactFriend(
        public val id: Long
    ) : AbstractPermissibleIdentifier(ExactUser(id)) {
        override fun toString(): String = "f$id"
    }

    public data class AnyTemp(
        public val groupId: Long,
    ) : AbstractPermissibleIdentifier(AnyUser, AnyMember(groupId)) {
        override fun toString(): String = "t$groupId.*"
    }

    public data class ExactTemp(
        public val groupId: Long,
        public val memberId: Long
    ) : AbstractPermissibleIdentifier(ExactUser(groupId), ExactMember(groupId, memberId)) {
        override fun toString(): String = "t$groupId.$memberId"
    }

    public object AnyUser : AbstractPermissibleIdentifier(AnyContact) {
        override fun toString(): String = "u*"
    }

    public data class ExactUser(
        public val id: Long
    ) : AbstractPermissibleIdentifier(AnyUser) {
        override fun toString(): String = "u$id"
    }

    public object AnyContact : AbstractPermissibleIdentifier() {
        override fun toString(): String = "*"
    }

    public object Console : AbstractPermissibleIdentifier() {
        override fun toString(): String = "console"
    }
}