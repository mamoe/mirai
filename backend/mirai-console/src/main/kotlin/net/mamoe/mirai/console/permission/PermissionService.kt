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
import net.mamoe.mirai.console.permission.PermissibleIdentifier.Companion.grantedWith
import java.util.concurrent.CopyOnWriteArrayList
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

    @Throws(DuplicatedRegistrationException::class)
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
public abstract class AbstractConcurrentPermissionService<P : Permission> : PermissionService<P> {
    protected abstract val permissions: MutableMap<PermissionId, P>
    protected abstract val grantedPermissionsMap: MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>

    protected abstract fun createPermission(
        id: PermissionId,
        description: String,
        base: PermissionId?
    ): P

    override fun get(id: PermissionId): P? = permissions[id]

    override fun register(id: PermissionId, description: String, base: PermissionId?): P {
        grantedPermissionsMap[id] = CopyOnWriteArrayList() // mutations are not quite often performed
        val instance = createPermission(id, description, base)
        if (permissions.putIfAbsent(id, instance) != null) {
            throw DuplicatedRegistrationException("Duplicated Permission registry. new: $instance, old: ${permissions[id]}")
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

    public override fun getGrantedPermissions(permissible: Permissible): Sequence<P> = sequence<P> {
        for ((permissionIdentifier, permissibleIdentifiers) in grantedPermissionsMap) {
            val myIdentifier = permissible.identifier

            val granted =
                if (permissibleIdentifiers.isEmpty()) false
                else permissibleIdentifiers.any { myIdentifier grantedWith it }

            if (granted) get(permissionIdentifier)?.let { yield(it) }
        }
    }
}

@ExperimentalPermission
public inline fun Permissible.getGrantedPermissions(): Sequence<Permission> =
    PermissionService.INSTANCE.run {
        getGrantedPermissions(this@getGrantedPermissions)
    }


@ExperimentalPermission
public inline fun Permission.testPermission(permissible: Permissible): Boolean =
    PermissionService.INSTANCE.run {
        require(permissionType.isInstance(this@testPermission)) {
            "Custom-constructed Permission instance is not allowed. " +
                    "Please obtain Permission from PermissionService.INSTANCE.register or PermissionService.INSTANCE.get"
        }

        @Suppress("UNCHECKED_CAST")
        this as PermissionService<Permission>

        testPermission(permissible, this@testPermission)
    }

@ExperimentalPermission
public inline fun PermissionId.testPermission(permissible: Permissible): Boolean {
    val p = PermissionService.INSTANCE[this] ?: return false
    return p.testPermission(permissible)
}