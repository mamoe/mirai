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

import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * [PermissionServiceProvider]
 */
@ExperimentalPermission
public interface PermissionService<P : Permission> {
    @ExperimentalPermission
    public val permissionType: KClass<P>
    public val rootPermission: P

    ///////////////////////////////////////////////////////////////////////////

    public operator fun get(id: PermissionId): P?

    public fun getRegisteredPermissions(): Sequence<P>
    public fun getGrantedPermissions(permissibleIdentifier: PermissibleIdentifier): Sequence<P>

    public fun testPermission(permissibleIdentifier: PermissibleIdentifier, permission: P): Boolean {
        val permissionId = permission.id
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
        parent: Permission = RootPermission,
    ): P

    ///////////////////////////////////////////////////////////////////////////

    public fun grant(permissibleIdentifier: PermissibleIdentifier, permission: P)
    public fun deny(permissibleIdentifier: PermissibleIdentifier, permission: P)

    public companion object {
        internal var instanceField: PermissionService<*>? = null

        @get:JvmName("getInstance")
        @JvmStatic
        public val INSTANCE: PermissionService<out Permission>
            get() = instanceField ?: error("PermissionService is not yet initialized therefore cannot be used.")

        public fun <P : Permission> PermissionService<P>.getOrFail(id: PermissionId): P =
            get(id) ?: throw PermissionNotFoundException(id)

        internal fun PermissionService<*>.allocatePermissionIdForPlugin(name: String, id: String) =
            PermissionId("plugin.${name.toLowerCase()}", id.toLowerCase())

        public fun PermissionId.findCorrespondingPermission(): Permission? = INSTANCE[this]

        public fun PermissionId.findCorrespondingPermissionOrFail(): Permission = INSTANCE.getOrFail(this)

        public fun PermissibleIdentifier.grantPermission(permission: Permission) {
            INSTANCE.checkType(permission::class).grant(this, permission)
        }

        public fun PermissibleIdentifier.grantPermission(permissionId: PermissionId) {
            grantPermission(permissionId.findCorrespondingPermissionOrFail())
        }

        public fun PermissibleIdentifier.denyPermission(permission: Permission) {
            INSTANCE.checkType(permission::class).deny(this, permission)
        }

        public fun PermissibleIdentifier.denyPermission(permissionId: PermissionId) {
            denyPermission(permissionId.findCorrespondingPermissionOrFail())
        }

        public fun Permissible.hasPermission(permission: Permission): Boolean =
            permission.testPermission(this@hasPermission)

        public fun PermissibleIdentifier.hasPermission(permission: Permission): Boolean =
            permission.testPermission(this@hasPermission)

        public fun PermissibleIdentifier.hasPermission(permissionId: PermissionId): Boolean {
            val instance = permissionId.findCorrespondingPermissionOrFail()
            return INSTANCE.checkType(instance::class).testPermission(this@hasPermission, instance)
        }

        public fun Permissible.hasPermission(permissionId: PermissionId): Boolean =
            permissionId.testPermission(this@hasPermission)

        public fun Permissible.getGrantedPermissions(): Sequence<Permission> =
            INSTANCE.getGrantedPermissions(this@getGrantedPermissions.identifier)

        public fun Permissible.grantPermission(vararg permissions: Permission) {
            for (permission in permissions) {
                INSTANCE.checkType(permission::class).grant(this.identifier, permission)
            }
        }

        public fun Permissible.denyPermission(vararg permissions: Permission) {
            for (permission in permissions) {
                INSTANCE.checkType(permission::class).deny(this.identifier, permission)
            }
        }

        public fun PermissibleIdentifier.getGrantedPermissions(): Sequence<Permission> =
            INSTANCE.getGrantedPermissions(this@getGrantedPermissions)

        public fun Permission.testPermission(permissible: Permissible): Boolean =
            INSTANCE.checkType(this::class).testPermission(permissible.identifier, this@testPermission)

        public fun Permission.testPermission(permissibleIdentifier: PermissibleIdentifier): Boolean =
            INSTANCE.checkType(this::class).testPermission(permissibleIdentifier, this@testPermission)

        public fun PermissionId.testPermission(permissible: Permissible): Boolean {
            val p = INSTANCE[this] ?: return false
            return p.testPermission(permissible)
        }

        public fun PermissionId.testPermission(permissible: PermissibleIdentifier): Boolean {
            val p = INSTANCE[this] ?: return false
            return p.testPermission(permissible)
        }
    }
}

@OptIn(ExperimentalPermission::class)
internal fun PermissionService<*>.checkType(permissionType: KClass<out Permission>): PermissionService<Permission> {
    return PermissionService.INSTANCE.run {
        require(this.permissionType.isSuperclassOf(permissionType)) {
            "Custom-constructed Permission instance is not allowed (Required ${this.permissionType}, found ${permissionType}. " +
                    "Please obtain Permission from PermissionService.INSTANCE.register or PermissionService.INSTANCE.get"
        }

        @Suppress("UNCHECKED_CAST")
        this as PermissionService<Permission>
    }
}
