/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.PluginDataExtensions.withDefault
import net.mamoe.mirai.console.data.value
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass


@ExperimentalPermission
internal object AllGrantPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionId, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl> get() = PermissionImpl::class
    override val rootPermission: PermissionImpl get() = RootPermissionImpl

    override fun register(
        id: PermissionId,
        description: String,
        parent: Permission
    ): PermissionImpl {
        val new = PermissionImpl(id, description, parent)
        val old = all.putIfAbsent(id, new)
        if (old != null) throw DuplicatedPermissionRegistrationException(new, old)
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
    override fun getRegisteredPermissions(): Sequence<PermissionImpl> = all.values.asSequence()
    override fun getGrantedPermissions(permissibleIdentifier: PermissibleIdentifier): Sequence<PermissionImpl> =
        all.values.asSequence()

    override fun grant(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }

    override fun testPermission(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl): Boolean =
        true

    override fun deny(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalPermission::class)
private val RootPermissionImpl = PermissionImpl(PermissionId("*", "*"), "The root permission").also { it.parent = it }

@ExperimentalPermission
internal object AllDenyPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionId, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class
    override val rootPermission: PermissionImpl get() = RootPermissionImpl

    override fun register(
        id: PermissionId,
        description: String,
        parent: Permission
    ): PermissionImpl {
        val new = PermissionImpl(id, description, parent)
        val old = all.putIfAbsent(id, new)
        if (old != null) throw DuplicatedPermissionRegistrationException(new, old)
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
    override fun getRegisteredPermissions(): Sequence<PermissionImpl> = all.values.asSequence()
    override fun getGrantedPermissions(permissibleIdentifier: PermissibleIdentifier): Sequence<PermissionImpl> =
        emptySequence()

    override fun grant(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }

    override fun testPermission(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl): Boolean =
        false

    override fun deny(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }
}

@ExperimentalPermission
internal object BuiltInPermissionService : AbstractConcurrentPermissionService<PermissionImpl>(),
    PermissionService<PermissionImpl> {

    @ExperimentalPermission
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class
    override val permissions: MutableMap<PermissionId, PermissionImpl> = ConcurrentHashMap()
    override val rootPermission: PermissionImpl
        get() = RootPermissionImpl

    @Suppress("UNCHECKED_CAST")
    override val grantedPermissionsMap: MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>
        get() = config.grantedPermissionMap as MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>

    override fun createPermission(id: PermissionId, description: String, parent: Permission): PermissionImpl =
        PermissionImpl(id, description, parent)

    internal val config: ConcurrentSaveData =
        ConcurrentSaveData("PermissionService")

    @Suppress("RedundantVisibilityModifier")
    @ExperimentalPermission
    internal class ConcurrentSaveData private constructor(
        public override val saveName: String,
        @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?
    ) : AutoSavePluginConfig() {
        public val grantedPermissionMap: MutableMap<PermissionId, MutableList<AbstractPermissibleIdentifier>>
                by value<MutableMap<PermissionId, MutableList<AbstractPermissibleIdentifier>>>(ConcurrentHashMap())
                    .withDefault { CopyOnWriteArrayList() }

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
@ExperimentalPermission
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
        if (parent != other.parent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + parent.hashCode()
        return result
    }

    override fun toString(): String = "PermissionImpl(id=$id, description='$description', parentId=$parent)"
}