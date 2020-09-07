/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


@ExperimentalPermission
public object AllGrantPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionIdentifier, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class

    override fun register(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier?
    ): PermissionImpl {
        val new = PermissionImpl(identifier, description, base)
        if (all.putIfAbsent(identifier, new) != null) {
            throw DuplicatedRegistrationException("Duplicated Permission registry: ${all[identifier]}")
        }
        return new
    }

    override fun get(identifier: PermissionIdentifier): PermissionImpl? = all[identifier]
    override fun getGrantedPermissions(permissible: Permissible): Sequence<PermissionImpl> = all.values.asSequence()
}

@ExperimentalPermission
public object AllDenyPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionIdentifier, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class

    override fun register(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier?
    ): PermissionImpl {
        val new = PermissionImpl(identifier, description, base)
        if (all.putIfAbsent(identifier, new) != null) {
            throw DuplicatedRegistrationException("Duplicated Permission registry: ${all[identifier]}")
        }
        return new
    }

    override fun get(identifier: PermissionIdentifier): PermissionImpl? = all[identifier]
    override fun getGrantedPermissions(permissible: Permissible): Sequence<PermissionImpl> = emptySequence()
}
