/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.permission

import net.mamoe.mirai.console.data.PluginDataExtensions
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.permission.PermitteeId.Companion.hasChild

internal abstract class AbstractConcurrentPermissionService<P : Permission> : PermissionService<P> {
    protected abstract val permissions: MutableMap<PermissionId, P>
    protected abstract val grantedPermissionsMap: PluginDataExtensions.NotNullMutableMap<PermissionId, MutableCollection<PermitteeId>>

    protected abstract fun createPermission(
        id: PermissionId,
        description: String,
        parent: Permission
    ): P

    override fun get(id: PermissionId): P? = permissions[id]

    override fun register(id: PermissionId, description: String, parent: Permission): P {
        val instance = createPermission(id, description, parent)
        val old = permissions.putIfAbsent(id, instance)
        if (old != null) throw PermissionRegistryConflictException(instance, old)
        return instance
    }

    override fun permit(permitteeId: PermitteeId, permission: P) {
        val id = permission.id
        grantedPermissionsMap[id].add(permitteeId)
    }

    override fun cancel(permitteeId: PermitteeId, permission: P, recursive: Boolean) {
        if (recursive) {
            grantedPermissionsMap[permission.id]
        } else grantedPermissionsMap[permission.id].remove(permitteeId)
    }

    override fun getRegisteredPermissions(): Sequence<P> = permissions.values.asSequence()
    override fun getPermittedPermissions(permitteeId: PermitteeId): Sequence<P> = sequence<P> {
        for ((permissionIdentifier, permissibleIdentifiers) in grantedPermissionsMap) {

            val granted =
                if (permissibleIdentifiers.isEmpty()) false
                else permissibleIdentifiers.any { permitteeId.hasChild(it) }

            if (granted) get(permissionIdentifier)?.let { yield(it) }
        }
    }
}