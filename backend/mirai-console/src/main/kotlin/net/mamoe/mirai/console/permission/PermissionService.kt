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

import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * [PermissionServiceProvider]
 */
@ExperimentalPermission
public interface PermissionService<P : Permission> {
    @ExperimentalPermission
    public val permissionType: KClass<P>

    @Throws(DuplicatedRegistrationException::class)
    public fun register(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier? = null
    ): P

    public operator fun get(identifier: PermissionIdentifier): P?

    public fun getGrantedPermissions(permissible: Permissible): Sequence<P>


    public fun testPermission(permissible: Permissible, permission: P): Boolean =
        permissible.getGrantedPermissions().any { it == permission }

    public companion object {
        private val builtIn: PermissionService<out Permission> get() = AllGrantPermissionService

        @get:JvmName("getInstance")
        @JvmStatic
        public val INSTANCE: PermissionService<out Permission> by lazy {
            PermissionServiceProvider.getExtensions().singleOrNull()?.extension?.instance ?: builtIn
            // TODO: 2020/9/4 ExtensionSelector
        }
    }
}

@ExperimentalPermission
public abstract class AbstractPermissionService<P : Permission> : PermissionService<P> {
    protected val all: MutableMap<PermissionIdentifier, P> = ConcurrentHashMap<PermissionIdentifier, P>()

    protected abstract fun createPermission(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier?
    ): P

    override fun register(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier?
    ): P {
        val new = createPermission(identifier, description, base)
        if (all.putIfAbsent(identifier, new) != null) {
            throw DuplicatedRegistrationException("Duplicated Permission registry: ${all[identifier]}")
        }
        return new
    }

    override fun get(identifier: PermissionIdentifier): P? = all[identifier]
    override fun getGrantedPermissions(permissible: Permissible): Sequence<P> = all.values.asSequence()
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
public inline fun PermissionIdentifier.testPermission(permissible: Permissible): Boolean {
    val p = PermissionService.INSTANCE[this] ?: return false
    return p.testPermission(permissible)
}