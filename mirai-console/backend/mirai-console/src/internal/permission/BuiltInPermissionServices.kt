/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.permission

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.PluginDataExtensions
import net.mamoe.mirai.console.data.PluginDataExtensions.withDefault
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.permission.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

@Suppress("unused") // don't pollute top-level
internal fun PermissionService<*>.checkType(permissionType: KClass<out Permission>): PermissionService<Permission> {
    require(this.permissionType.isSuperclassOf(permissionType)) {
        "Custom-constructed Permission instance is not allowed (Required ${this.permissionType}, found ${permissionType}. " +
                "Please obtain Permission from PermissionService.INSTANCE.register or PermissionService.INSTANCE.get"
    }

    @Suppress("UNCHECKED_CAST")
    return this as PermissionService<Permission>
}

internal class AllPermitPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionId, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl> get() = PermissionImpl::class
    override val rootPermission: PermissionImpl get() = RootPermissionImpl.also { all[it.id] = it }

    override fun register(
        id: PermissionId,
        description: String,
        parent: Permission,
    ): PermissionImpl {
        val new = PermissionImpl(id, description, parent)
        val old = all.putIfAbsent(id, new)
        if (old != null) throw PermissionRegistryConflictException(new, old)
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
    override fun getRegisteredPermissions(): Sequence<PermissionImpl> = all.values.asSequence()
    override fun getPermittedPermissions(permitteeId: PermitteeId): Sequence<PermissionImpl> =
        all.values.asSequence()

    override fun permit(permitteeId: PermitteeId, permission: PermissionImpl) {
    }

    override fun testPermission(permitteeId: PermitteeId, permission: PermissionImpl): Boolean =
        true

    override fun cancel(permitteeId: PermitteeId, permission: PermissionImpl, recursive: Boolean) {
    }
}

@Suppress("DEPRECATION")
private val RootPermissionImpl = PermissionImpl(PermissionId("*", "*"), "The root permission").also { it.parent = it }

internal class AllDenyPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionId, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class
    override val rootPermission: PermissionImpl = RootPermissionImpl.also { all[it.id] = it }

    override fun register(
        id: PermissionId,
        description: String,
        parent: Permission,
    ): PermissionImpl {
        val new = PermissionImpl(id, description, parent)
        val old = all.putIfAbsent(id, new)
        if (old != null) throw PermissionRegistryConflictException(new, old)
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
    override fun getRegisteredPermissions(): Sequence<PermissionImpl> = all.values.asSequence()
    override fun getPermittedPermissions(permitteeId: PermitteeId): Sequence<PermissionImpl> =
        emptySequence()

    override fun permit(permitteeId: PermitteeId, permission: PermissionImpl) {
    }

    override fun testPermission(permitteeId: PermitteeId, permission: PermissionImpl): Boolean =
        false

    override fun cancel(permitteeId: PermitteeId, permission: PermissionImpl, recursive: Boolean) {
    }
}

internal class BuiltInPermissionService : AbstractConcurrentPermissionService<PermissionImpl>(),
    PermissionService<PermissionImpl> {

    class Provider : PermissionServiceProvider {
        override val instance: PermissionService<*> by lazy {
            BuiltInPermissionService()
        }
    }

    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class
    override val permissions: ConcurrentHashMap<PermissionId, PermissionImpl> = ConcurrentHashMap()
    override val rootPermission: PermissionImpl = RootPermissionImpl.also { permissions[it.id] = it }

    @Suppress("UNCHECKED_CAST")
    override val grantedPermissionsMap: PluginDataExtensions.NotNullMutableMap<PermissionId, MutableCollection<PermitteeId>>
        get() = config.grantedPermissionMap as PluginDataExtensions.NotNullMutableMap<PermissionId, MutableCollection<PermitteeId>>

    override fun createPermission(id: PermissionId, description: String, parent: Permission): PermissionImpl =
        PermissionImpl(id, description, parent)

    internal val config: ConcurrentSaveData =
        ConcurrentSaveData("PermissionService")

    @Suppress("RedundantVisibilityModifier")
    internal class ConcurrentSaveData private constructor(
        saveName: String,
        @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?,
    ) : AutoSavePluginConfig(saveName) {
        public val grantedPermissionMap: PluginDataExtensions.NotNullMutableMap<PermissionId, MutableSet<AbstractPermitteeId>>
                by value<MutableMap<PermissionId, MutableSet<AbstractPermitteeId>>>(ConcurrentHashMap())
                    .withDefault { CopyOnWriteArraySet() }

        public companion object {
            @JvmStatic
            public operator fun invoke(
                saveName: String,
                // delegate: PluginConfig,
            ): ConcurrentSaveData = ConcurrentSaveData(saveName, null)
        }
    }
}

/**
 * [Permission] 的简单实现
 */
@Serializable
internal data class PermissionImpl @Deprecated("Only for Root") constructor(
    override val id: PermissionId,
    override val description: String,
) : Permission {
    override lateinit var parent: Permission

    @Suppress("DEPRECATION")
    constructor(id: PermissionId, description: String, parent: Permission) : this(id, description) {
        this.parent = parent
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionImpl

        if (id != other.id) return false
        if (description != other.description) return false
        if (parent !== other.parent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + if (parent == this) 1 else parent.hashCode()
        return result
    }

    override fun toString(): String =
        "PermissionImpl(id=$id, description='$description', parent=${if (parent === this) "<self>" else parent.toString()})"
}