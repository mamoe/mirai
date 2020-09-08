/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.extension.SingletonExtensionPoint.Companion.findSingleton
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import kotlin.reflect.KClass

/**
 * [PermissionServiceProvider]
 */
@ExperimentalPermission
public interface PermissionService<P : Permission> {
    @ExperimentalPermission
    public val permissionType: KClass<P>

    ///////////////////////////////////////////////////////////////////////////

    public operator fun get(id: PermissionId): P?

    public fun getGrantedPermissions(permissible: Permissible): Sequence<P>

    public fun testPermission(permissible: Permissible, permission: P): Boolean =
        permissible.getGrantedPermissions().any { it == permission }


    ///////////////////////////////////////////////////////////////////////////

    @Throws(DuplicatedPermissionRegistrationException::class)
    public fun register(
        id: PermissionId,
        description: String,
        base: PermissionId? = null
    ): P

    ///////////////////////////////////////////////////////////////////////////

    public fun grant(permissibleIdentifier: PermissibleIdentifier, permission: P)
    public fun deny(permissibleIdentifier: PermissibleIdentifier, permission: P)

    public companion object {
        @get:JvmName("getInstance")
        @JvmStatic
        public val INSTANCE: PermissionService<out Permission> by lazy {
            PermissionServiceProvider.findSingleton()?.instance ?: BuiltInPermissionService
        }
    }
}

@ExperimentalPermission
public inline fun Permissible.hasPermission(permission: Permission): Boolean =
    PermissionService.run { permission.testPermission(this@hasPermission) }

@ExperimentalPermission
public inline fun Permissible.hasPermission(permission: PermissionId): Boolean =
    PermissionService.run { permission.testPermission(this@hasPermission) }

@JvmSynthetic
@ExperimentalPermission
public inline fun Permissible.getGrantedPermissions(): Sequence<Permission> =
    PermissionService.INSTANCE.run {
        getGrantedPermissions(this@getGrantedPermissions)
    }

@JvmSynthetic
@ExperimentalPermission
public fun Permission.testPermission(permissible: Permissible): Boolean =
    PermissionService.INSTANCE.run {
        require(permissionType.isInstance(this@testPermission)) {
            "Custom-constructed Permission instance is not allowed. " +
                    "Please obtain Permission from PermissionService.INSTANCE.register or PermissionService.INSTANCE.get"
        }

        @Suppress("UNCHECKED_CAST")
        this as PermissionService<Permission>

        testPermission(permissible, this@testPermission)
    }

@JvmSynthetic
@ExperimentalPermission
public fun PermissionId.testPermission(permissible: Permissible): Boolean {
    val p = PermissionService.INSTANCE[this] ?: return false
    return p.testPermission(permissible)
}