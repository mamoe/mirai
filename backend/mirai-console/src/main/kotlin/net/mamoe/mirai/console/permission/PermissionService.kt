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

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.Value
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * [PermissionServiceProvider]
 */
@ExperimentalPermission
public interface PermissionService<P : Permission> {
    public fun register(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier? = null
    ): P

    public operator fun get(identifier: PermissionIdentifier): P?


    public fun getGrantedPermissions(permissible: Permissible): List<PermissionIdentifier>


    public fun testPermission(permissible: Permissible, permission: P): Boolean =
        permissible.getGrantedPermissions().any { it == permission.identifier }


    public companion object INSTANCE : PermissionService<Permission> {
        private val builtIn: PermissionService<out Permission> get() = TODO("PS IMPL")

        @Suppress("UNCHECKED_CAST")
        private val instance by lazy {
            PermissionServiceProvider.getExtensions().singleOrNull()?.extension?.instance
                ?: builtIn  // TODO: 2020/9/4 ask for further choice
                        as PermissionService<Permission>
        }

        override fun register(
            identifier: PermissionIdentifier,
            description: String,
            base: PermissionIdentifier?
        ): Permission = instance.register(identifier, description, base)

        override fun get(identifier: PermissionIdentifier): Permission? = instance[identifier]
        override fun getGrantedPermissions(permissible: Permissible): List<PermissionIdentifier> =
            instance.getGrantedPermissions(permissible)
    }
}

@ExperimentalPermission
public interface HotDeploymentSupportPermissionService<P : Permission> : PermissionService<P> {
    public fun grant(permissible: Permissible, permission: P)
    public fun deny(permissible: Permissible, permission: P)
}

@ExperimentalPermission
public open class HotDeploymentNotSupportedException : Exception {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}


/**
 * [PermissionServiceProvider]
 */
@ExperimentalPermission
public abstract class AbstractPermissionService<P : Permission> : AutoSavePluginConfig(), PermissionService<P> {
    @JvmField
    protected val permissions: ConcurrentLinkedQueue<P> = ConcurrentLinkedQueue()

    @JvmField
    protected val grantedPermissionMap: Value<MutableMap<String, MutableList<PermissionIdentifier>>> = value()

    public override fun getGrantedPermissions(permissible: Permissible): List<PermissionIdentifier> =
        grantedPermissionMap.value[permissible.identifier].orEmpty()

    public override operator fun get(identifier: PermissionIdentifier): P? =
        permissions.find { it.identifier == identifier }

    public override fun testPermission(permissible: Permissible, permission: P): Boolean =
        permissible.getGrantedPermissions().any { it == permission.identifier }
}

@ExperimentalPermission
public inline fun Permissible.getGrantedPermissions(): List<PermissionIdentifier> =
    PermissionService.run { this.getGrantedPermissions(this@getGrantedPermissions) }

@ExperimentalPermission
public inline fun Permission.testPermission(permissible: Permissible): Boolean =
    PermissionService.run { testPermission(permissible, this@testPermission) }

@ExperimentalPermission
public inline fun PermissionIdentifier.testPermission(permissible: Permissible): Boolean {
    val p = PermissionService[this] ?: return false
    return p.testPermission(permissible)
}

@OptIn(ExperimentalPermission::class)
private class PermissionServiceImpl : AbstractPermissionService<PermissionServiceImpl.PermissionImpl>() {
    private val instances: ConcurrentLinkedQueue<PermissionImpl> = ConcurrentLinkedQueue()

    private class PermissionImpl(
        override val identifier: PermissionIdentifier,
        override val description: String,
        override val base: PermissionIdentifier?
    ) : Permission

    override fun register(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier?
    ): PermissionImpl = PermissionImpl(identifier, description, base)
}