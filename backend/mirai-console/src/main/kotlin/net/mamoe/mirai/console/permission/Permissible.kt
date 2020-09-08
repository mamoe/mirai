/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.command.CommandSender

/**
 * 可拥有权限的对象.
 *
 * 典型的实例为 [CommandSender]
 *
 * 注意: 请不要自主实现 [Permissible]
 *
 * @see CommandSender
 */
@ExperimentalPermission
public interface Permissible {
    public val identifier: PermissibleIdentifier
}


@ExperimentalPermission("Classname is subject to change")
public interface PermissibleIdentifier {
    public val parents: Array<out PermissibleIdentifier>

    public companion object {
        @ExperimentalPermission
        public infix fun PermissibleIdentifier.grantedWith(with: PermissibleIdentifier): Boolean {
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

/**
 *
 */
@ExperimentalPermission
public sealed class AbstractPermissibleIdentifier(
    public final override vararg val parents: PermissibleIdentifier
) : PermissibleIdentifier {
    public object AnyGroup : AbstractPermissibleIdentifier(AnyContact)
    public data class ExactGroup(public val groupId: Long) : AbstractPermissibleIdentifier(AnyGroup)

    public data class AnyMember(public val groupId: Long) : AbstractPermissibleIdentifier(AnyMemberFromAnyGroup)
    public object AnyMemberFromAnyGroup : AbstractPermissibleIdentifier(AnyUser)
    public data class ExactMember(
        public val groupId: Long,
        public val memberId: Long
    ) : AbstractPermissibleIdentifier(AnyMember(groupId), ExactUser(memberId))

    public object AnyFriend : AbstractPermissibleIdentifier(AnyUser)
    public data class ExactFriend(
        public val id: Long
    ) : AbstractPermissibleIdentifier(ExactUser(id))

    public object AnyTemp : AbstractPermissibleIdentifier(AnyUser)
    public data class ExactTemp(
        public val groupId: Long,
        public val id: Long
    ) : AbstractPermissibleIdentifier(ExactUser(groupId)) // TODO: 2020/9/8 ExactMember ?

    public object AnyUser : AbstractPermissibleIdentifier(AnyContact)
    public data class ExactUser(
        public val id: Long
    ) : AbstractPermissibleIdentifier(AnyUser)

    public object AnyContact : AbstractPermissibleIdentifier()

    public object Console : AbstractPermissibleIdentifier()
}

@ExperimentalPermission
public inline fun Permissible.hasPermission(permission: Permission): Boolean =
    PermissionService.run { permission.testPermission(this@hasPermission) }

@ExperimentalPermission
public inline fun Permissible.hasPermission(permission: PermissionId): Boolean =
    PermissionService.run { permission.testPermission(this@hasPermission) }
