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

    public fun getGrantedPermissions(permissibleIdentifier: PermissibleIdentifier): Sequence<P>

    public fun testPermission(permissibleIdentifier: PermissibleIdentifier, permissionId: PermissionId): Boolean {
        val all = this[permissionId]?.parentsWithSelfSequence() ?: return false
        return getGrantedPermissions(permissibleIdentifier).any { p ->
            all.any { p.id == it.id }
        }
    }


    ///////////////////////////////////////////////////////////////////////////

    @Throws(DuplicatedPermissionRegistrationException::class)
    public fun register(
        id: PermissionId,
        description: String,
        base: PermissionId = BasePermission.id
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
internal fun PermissionService<*>.allocatePermissionIdForPlugin(name: String, id: String) =
    PermissionId("plugin.${name.toLowerCase()}", id.toLowerCase())

@ExperimentalPermission
public fun PermissionId.findCorrespondingPermission(): Permission? = PermissionService.INSTANCE[this]

@ExperimentalPermission
public fun PermissibleIdentifier.grant(permission: Permission) {
    PermissionService.INSTANCE.checkType(permission::class).grant(this, permission)
}

@ExperimentalPermission
public fun Permissible.hasPermission(permission: Permission): Boolean =
    permission.testPermission(this@hasPermission)

@ExperimentalPermission
public fun PermissibleIdentifier.hasPermission(permission: Permission): Boolean =
    permission.testPermission(this@hasPermission)

@Suppress("UNCHECKED_CAST")
@ExperimentalPermission
public fun PermissibleIdentifier.hasPermission(permissionId: PermissionId): Boolean =
    (PermissionService.INSTANCE as PermissionService<Permission>).run {
        testPermission(this@hasPermission, permissionId)
    }

@ExperimentalPermission
public fun Permissible.hasPermission(permissionId: PermissionId): Boolean =
    permissionId.testPermission(this@hasPermission)

@JvmSynthetic
@ExperimentalPermission
public fun Permissible.getGrantedPermissions(): Sequence<Permission> =
    PermissionService.INSTANCE.getGrantedPermissions(this@getGrantedPermissions.identifier)

@JvmSynthetic
@ExperimentalPermission
public fun PermissibleIdentifier.getGrantedPermissions(): Sequence<Permission> =
    PermissionService.INSTANCE.getGrantedPermissions(this@getGrantedPermissions)

@JvmSynthetic
@ExperimentalPermission
public fun Permission.testPermission(permissible: Permissible): Boolean =
    PermissionService.INSTANCE.checkType(this::class).testPermission(permissible.identifier, this@testPermission.id)

@JvmSynthetic
@ExperimentalPermission
public fun Permission.testPermission(permissibleIdentifier: PermissibleIdentifier): Boolean =
    PermissionService.INSTANCE.checkType(this::class).testPermission(permissibleIdentifier, this@testPermission.id)

@JvmSynthetic
@ExperimentalPermission
public fun PermissionId.testPermission(permissible: Permissible): Boolean {
    val p = PermissionService.INSTANCE[this] ?: return false
    return p.testPermission(permissible)
}

@JvmSynthetic
@ExperimentalPermission
public fun PermissionId.testPermission(permissible: PermissibleIdentifier): Boolean {
    val p = PermissionService.INSTANCE[this] ?: return false
    return p.testPermission(permissible)
}

@OptIn(ExperimentalPermission::class)
internal fun PermissionService<*>.checkType(permissionType: KClass<out Permission>): PermissionService<Permission> {
    return PermissionService.INSTANCE.run {
        require(permissionType.isInstance(this@checkType)) {
            "Custom-constructed Permission instance is not allowed. " +
                    "Please obtain Permission from PermissionService.INSTANCE.register or PermissionService.INSTANCE.get"
        }

        @Suppress("UNCHECKED_CAST")
        this as PermissionService<Permission>
    }
}
