/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createType


@ExperimentalPermission
public object AllGrantPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionId, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class

    override fun register(
        id: PermissionId,
        description: String,
        base: PermissionId?
    ): PermissionImpl {
        val new = PermissionImpl(id, description, base)
        if (all.putIfAbsent(id, new) != null) {
            throw DuplicatedRegistrationException("Duplicated Permission registry: ${all[id]}")
        }
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
    override fun getGrantedPermissions(permissible: Permissible): Sequence<PermissionImpl> = all.values.asSequence()
    override fun grant(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }

    override fun testPermission(permissible: Permissible, permission: PermissionImpl): Boolean = true
    override fun deny(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }
}

@ExperimentalPermission
public object AllDenyPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionId, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class

    override fun register(
        id: PermissionId,
        description: String,
        base: PermissionId?
    ): PermissionImpl {
        val new = PermissionImpl(id, description, base)
        if (all.putIfAbsent(id, new) != null) {
            throw DuplicatedRegistrationException("Duplicated Permission registry: ${all[id]}")
        }
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
    override fun getGrantedPermissions(permissible: Permissible): Sequence<PermissionImpl> = emptySequence()
    override fun grant(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }

    override fun testPermission(permissible: Permissible, permission: PermissionImpl): Boolean = false
    override fun deny(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }
}

@ExperimentalPermission
internal object BuiltInPermissionService : AbstractConcurrentPermissionService<PermissionImpl>(),
    StorablePermissionService<PermissionImpl> {

    @ExperimentalPermission
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class
    override val permissions: MutableMap<PermissionId, PermissionImpl> get() = config.permissions

    @Suppress("UNCHECKED_CAST")
    override val grantedPermissionsMap: MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>
        get() = config.grantedPermissionMap as MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>

    override fun createPermission(id: PermissionId, description: String, base: PermissionId?): PermissionImpl =
        PermissionImpl(id, description, base)

    override val config: StorablePermissionService.ConcurrentSaveData<PermissionImpl> =
        StorablePermissionService.ConcurrentSaveData(
            PermissionImpl::class.createType(),
            "PermissionService",
            AutoSavePluginConfig()
        )
}