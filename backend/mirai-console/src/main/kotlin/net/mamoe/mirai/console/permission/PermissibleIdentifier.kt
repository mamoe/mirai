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
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

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
    internal companion object {
        val objects by lazy {
            // https://youtrack.jetbrains.com/issue/KT-41782
            AbstractPermissibleIdentifier::class.nestedClasses.mapNotNull { it.objectInstance }
        }

        val regexes: List<Pair<Regex, (matchGroup: MatchResult.Destructured) -> AbstractPermissibleIdentifier>> =
            listOf(
                Regex("""ExactGroup\(\s*([0-9]+)\s*\)""") to { (id) -> ExactGroup(id.toLong()) },
                Regex("""ExactFriend\(\s*([0-9]+)\s*\)""") to { (id) -> ExactFriend(id.toLong()) },
                Regex("""ExactUser\(\s*([0-9]+)\s*\)""") to { (id) -> ExactUser(id.toLong()) },
                Regex("""AnyMember\(\s*([0-9]+)\s*\)""") to { (id) -> AnyMember(id.toLong()) },
                Regex("""ExactMember\(\s*([0-9]+)\s*([0-9]+)\s*\)""") to { (a, b) ->
                    ExactMember(
                        a.toLong(),
                        b.toLong()
                    )
                },
                Regex("""ExactTemp\(\s*([0-9]+)\s*([0-9]+)\s*\)""") to { (a, b) -> ExactTemp(a.toLong(), b.toLong()) },
            )
    }

    @ConsoleExperimentalAPI
    public object AsStringSerializer : KSerializer<AbstractPermissibleIdentifier> by String.serializer().map(
        serializer = { it.toString() },

        deserializer = d@{ str ->
            @Suppress("NAME_SHADOWING") val str = str.trim()
            objects.find { it.toString() == str }?.let { return@d it as AbstractPermissibleIdentifier }
            for ((regex, block) in regexes) {
                val result = regex.find(str) ?: continue
                if (result.range.last != str.lastIndex) continue
                if (result.range.first != 0) continue
                return@d result.destructured.run(block)
            }
            error("Cannot deserialize '$str' as AbstractPermissibleIdentifier")
        }
    )

    public object AnyGroup : AbstractPermissibleIdentifier(AnyContact) {
        override fun toString(): String = "AnyGroup"
    }

    public data class ExactGroup(public val groupId: Long) : AbstractPermissibleIdentifier(AnyGroup)

    public data class AnyMember(public val groupId: Long) : AbstractPermissibleIdentifier(AnyMemberFromAnyGroup)

    public object AnyMemberFromAnyGroup : AbstractPermissibleIdentifier(AnyUser) {
        override fun toString(): String = "AnyMemberFromAnyGroup"
    }

    public data class ExactMember(
        public val groupId: Long,
        public val memberId: Long
    ) : AbstractPermissibleIdentifier(AnyMember(groupId), ExactUser(memberId))

    public object AnyFriend : AbstractPermissibleIdentifier(AnyUser) {
        override fun toString(): String = "AnyFriend"
    }

    public data class ExactFriend(
        public val id: Long
    ) : AbstractPermissibleIdentifier(ExactUser(id)) {
        override fun toString(): String = "ExactFriend"
    }

    public object AnyTemp : AbstractPermissibleIdentifier(AnyUser) {
        override fun toString(): String = "AnyTemp"
    }

    public data class ExactTemp(
        public val groupId: Long,
        public val id: Long
    ) : AbstractPermissibleIdentifier(ExactUser(groupId)) // TODO: 2020/9/8 ExactMember ?

    public object AnyUser : AbstractPermissibleIdentifier(AnyContact) {
        override fun toString(): String = "AnyUser"
    }

    public data class ExactUser(
        public val id: Long
    ) : AbstractPermissibleIdentifier(AnyUser)

    public object AnyContact : AbstractPermissibleIdentifier() {
        override fun toString(): String = "AnyContact"
    }

    public object Console : AbstractPermissibleIdentifier() {
        override fun toString(): String = "Console"
    }
}