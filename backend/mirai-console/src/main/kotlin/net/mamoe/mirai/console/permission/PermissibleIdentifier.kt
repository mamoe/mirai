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

import kotlinx.serialization.Serializable

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

        internal fun PermissibleIdentifier.allParentsWithSelf(): Sequence<PermissibleIdentifier> {
            return sequence {
                yield(this@allParentsWithSelf)
                yieldAll(parents.asSequence())
            }
        }
    }
}

@Serializable
@ExperimentalPermission
public sealed class AbstractPermissibleIdentifier(
    public final override vararg val parents: PermissibleIdentifier
) : PermissibleIdentifier {
    @Serializable
    public object AnyGroup : AbstractPermissibleIdentifier(AnyContact)

    @Serializable
    public data class ExactGroup(public val groupId: Long) : AbstractPermissibleIdentifier(AnyGroup)

    @Serializable
    public data class AnyMember(public val groupId: Long) : AbstractPermissibleIdentifier(AnyMemberFromAnyGroup)

    @Serializable
    public object AnyMemberFromAnyGroup : AbstractPermissibleIdentifier(AnyUser)

    @Serializable
    public data class ExactMember(
        public val groupId: Long,
        public val memberId: Long
    ) : AbstractPermissibleIdentifier(AnyMember(groupId), ExactUser(memberId))

    @Serializable
    public object AnyFriend : AbstractPermissibleIdentifier(AnyUser)

    @Serializable
    public data class ExactFriend(
        public val id: Long
    ) : AbstractPermissibleIdentifier(ExactUser(id))

    @Serializable
    public object AnyTemp : AbstractPermissibleIdentifier(AnyUser)

    @Serializable
    public data class ExactTemp(
        public val groupId: Long,
        public val id: Long
    ) : AbstractPermissibleIdentifier(ExactUser(groupId)) // TODO: 2020/9/8 ExactMember ?

    @Serializable
    public object AnyUser : AbstractPermissibleIdentifier(AnyContact)

    @Serializable
    public data class ExactUser(
        public val id: Long
    ) : AbstractPermissibleIdentifier(AnyUser)

    @Serializable
    public object AnyContact : AbstractPermissibleIdentifier()

    @Serializable
    public object Console : AbstractPermissibleIdentifier()
}