/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.permission.PermissibleIdentifier.Companion.grantedWith
import java.util.concurrent.CopyOnWriteArrayList

@ExperimentalPermission
public abstract class AbstractConcurrentPermissionService<P : Permission> : PermissionService<P> {
    protected abstract val permissions: MutableMap<PermissionId, P>
    protected abstract val grantedPermissionsMap: MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>

    protected abstract fun createPermission(
        id: PermissionId,
        description: String,
        base: PermissionId = BasePermission.id
    ): P

    override fun get(id: PermissionId): P? = permissions[id]

    override fun register(id: PermissionId, description: String, base: PermissionId): P {
        grantedPermissionsMap[id] = CopyOnWriteArrayList() // mutations are not quite often performed
        val instance = createPermission(id, description, base)
        if (permissions.putIfAbsent(id, instance) != null) {
            throw DuplicatedPermissionRegistrationException("Duplicated Permission registry. new: $instance, old: ${permissions[id]}")
        }
        return instance
    }

    override fun grant(permissibleIdentifier: PermissibleIdentifier, permission: P) {
        val id = permission.id
        grantedPermissionsMap[id]?.add(permissibleIdentifier)
            ?: error("Bad PermissionService implementation: grantedPermissionsMap[id] is null.")
    }

    override fun deny(permissibleIdentifier: PermissibleIdentifier, permission: P) {
        grantedPermissionsMap[permission.id]?.remove(permissibleIdentifier)
    }

    public override fun getGrantedPermissions(permissibleIdentifier: PermissibleIdentifier): Sequence<P> = sequence<P> {
        for ((permissionIdentifier, permissibleIdentifiers) in grantedPermissionsMap) {

            val granted =
                if (permissibleIdentifiers.isEmpty()) false
                else permissibleIdentifiers.any { permissibleIdentifier.grantedWith(it) }

            if (granted) get(permissionIdentifier)?.let { yield(it) }
        }
    }
}