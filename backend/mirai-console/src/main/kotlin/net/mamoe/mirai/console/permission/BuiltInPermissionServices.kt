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
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginDataExtensions.withDefault
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.data.valueFromKType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType


@ExperimentalPermission
public object AllGrantPermissionService : PermissionService<PermissionImpl> {
    private val all = ConcurrentHashMap<PermissionId, PermissionImpl>()
    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class

    override fun register(
        id: PermissionId,
        description: String,
        base: PermissionId
    ): PermissionImpl {
        val new = PermissionImpl(id, description, base)
        if (all.putIfAbsent(id, new) != null) {
            throw DuplicatedPermissionRegistrationException("Duplicated Permission registry: ${all[id]}")
        }
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
    override fun getGrantedPermissions(permissibleIdentifier: PermissibleIdentifier): Sequence<PermissionImpl> =
        all.values.asSequence()

    override fun grant(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl) {
    }

    override fun testPermission(permissibleIdentifier: PermissibleIdentifier, permission: PermissionImpl): Boolean =
        true

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
        base: PermissionId
    ): PermissionImpl {
        val new = PermissionImpl(id, description, base)
        if (all.putIfAbsent(id, new) != null) {
            throw DuplicatedPermissionRegistrationException("Duplicated Permission registry: ${all[id]}")
        }
        return new
    }

    override fun get(id: PermissionId): PermissionImpl? = all[id]
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
    override val permissions: MutableMap<PermissionId, PermissionImpl> get() = config.permissions

    @Suppress("UNCHECKED_CAST")
    override val grantedPermissionsMap: MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>
        get() = config.grantedPermissionMap as MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>

    override fun createPermission(id: PermissionId, description: String, base: PermissionId): PermissionImpl =
        PermissionImpl(id, description, base)

    internal val config: ConcurrentSaveData<PermissionImpl> =
        ConcurrentSaveData(
            PermissionImpl::class.createType(),
            "PermissionService",
            AutoSavePluginConfig()
        )

    @Suppress("RedundantVisibilityModifier")
    @ExperimentalPermission
    internal class ConcurrentSaveData<P : Permission> private constructor(
        permissionType: KType,
        public override val saveName: String,
        delegate: PluginConfig,
        @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?
    ) : PluginConfig by delegate {
        public val permissions: MutableMap<PermissionId, P>
                by valueFromKType<MutableMap<PermissionId, P>>(
                    MutableMap::class.createType(
                        listOf(
                            KTypeProjection(KVariance.INVARIANT, PermissionId::class.createType()),
                            KTypeProjection(KVariance.INVARIANT, permissionType),
                        )
                    ),
                    ConcurrentHashMap()
                )

        public val grantedPermissionMap: MutableMap<PermissionId, List<PermissibleIdentifier>>
                by value<MutableMap<PermissionId, List<PermissibleIdentifier>>>(ConcurrentHashMap())
                    .withDefault { CopyOnWriteArrayList() }

        public companion object {
            @JvmStatic
            public operator fun <P : Permission> invoke(
                permissionType: KType,
                saveName: String,
                delegate: PluginConfig,
            ): ConcurrentSaveData<P> = ConcurrentSaveData(permissionType, saveName, delegate, null)
        }
    }
}