/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.internal.permission.checkType
import net.mamoe.mirai.console.permission.Permission.Companion.parentsWithSelf
import kotlin.reflect.KClass

/**
 * 权限服务. 用于承载 Console 的权限系统.
 *
 * ### 可扩展
 * 权限服务可由插件扩展并覆盖默认实现.
 *
 * [PermissionServiceProvider]
 */
@PermissionImplementation
public interface PermissionService<P : Permission> {
    /**
     * [P] 的类型
     */
    public val permissionType: KClass<P>

    /**
     * [RootPermission] 的实现
     */
    public val rootPermission: P

    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取一个已经 [注册][register] 了的 [P]
     */
    public operator fun get(id: PermissionId): P?

    /**
     * 获取所有已注册的指令列表. 应保证线程安全.
     */
    public fun getRegisteredPermissions(): Sequence<P>

    /**
     * 获取 [PermitteeId] 和其父标识的所有被授予的所有直接和间接的权限列表
     */
    public fun getPermittedPermissions(permitteeId: PermitteeId): Sequence<P>

    /**
     * 判断 [permission] 对 [permission] 的权限.
     *
     * 返回 `true` 的意义:
     * - 通常意义: [permitteeId] 拥有 [permission] 的 '能力'
     * - 实现意义: [permitteeId] 自身或任意父标识 [PermissionService] 被授予高于或等于 [permission] 的权限
     *
     * @see Companion.testPermission 接收 [Permittee] 参数的扩展
     */
    public fun testPermission(permitteeId: PermitteeId, permission: P): Boolean {
        val permissionId = permission.id
        val all = this[permissionId]?.parentsWithSelf ?: return false
        return getPermittedPermissions(permitteeId).any { p ->
            all.any { p.id == it.id }
        }
    }


    ///////////////////////////////////////////////////////////////////////////

    /**
     * 申请并注册一个权限 [Permission].
     *
     * @throws PermissionRegistryConflictException 当已存在一个 [PermissionId] 时抛出.
     */
    @Throws(PermissionRegistryConflictException::class)
    public fun register(
        id: PermissionId,
        description: String,
        parent: Permission = RootPermission,
    ): P

    ///////////////////////////////////////////////////////////////////////////

    /**
     * 授予 [permitteeId] 以 [permission] 权限
     *
     * Console 内建的权限服务支持授予操作. 但插件扩展的权限服务可能不支持.
     *
     * @throws UnsupportedOperationException 当插件扩展的 [PermissionService] 不支持这样的操作时抛出.
     */
    public fun permit(permitteeId: PermitteeId, permission: P)

    /**
     * 撤销 [permitteeId] 的 [permission] 授权
     *
     * Console 内建的权限服务支持授予操作. 但插件扩展的权限服务可能不支持.
     *
     * @param recursive `true` 时递归撤销所有子权限.
     * 例如, 若 [permission] 为 "*:*",
     * recursive 为 `true` 时撤销全部权限 (因为所有权限都是 "*:*" 的子权限);
     * 而为 `false` 时仅撤销 "*:*" 本身, 而不会影响子权限.
     *
     * @throws UnsupportedOperationException 当插件扩展的 [PermissionService] 不支持这样的操作时抛出.
     */
    public fun cancel(permitteeId: PermitteeId, permission: P, recursive: Boolean)

    public companion object {
        internal var instanceField: PermissionService<*>? = null

        @get:JvmName("getInstance")
        @JvmStatic
        public val INSTANCE: PermissionService<out Permission>
            get() = instanceField ?: error("PermissionService is not yet initialized therefore cannot be used.")

        public fun <P : Permission> PermissionService<P>.getOrFail(id: PermissionId): P =
            get(id) ?: throw NoSuchElementException("Permission not found: $id")

        internal fun PermissionService<*>.allocatePermissionIdForPlugin(name: String, id: String) =
            PermissionId("plugin.${name.toLowerCase()}", id.toLowerCase())

        public fun PermissionId.findCorrespondingPermission(): Permission? = INSTANCE[this]

        public fun PermissionId.findCorrespondingPermissionOrFail(): Permission = INSTANCE.getOrFail(this)

        public fun PermitteeId.grantPermission(permission: Permission) {
            INSTANCE.checkType(permission::class).permit(this, permission)
        }

        public fun PermitteeId.grantPermission(permissionId: PermissionId) {
            grantPermission(permissionId.findCorrespondingPermissionOrFail())
        }

        public fun PermitteeId.denyPermission(permission: Permission, recursive: Boolean) {
            INSTANCE.checkType(permission::class).cancel(this, permission, recursive)
        }

        public fun PermitteeId.denyPermission(permissionId: PermissionId, recursive: Boolean) {
            denyPermission(permissionId.findCorrespondingPermissionOrFail(), recursive)
        }

        public fun Permittee.hasPermission(permission: Permission): Boolean =
            permission.testPermission(this@hasPermission)

        public fun PermitteeId.hasPermission(permission: Permission): Boolean =
            permission.testPermission(this@hasPermission)

        public fun PermitteeId.hasPermission(permissionId: PermissionId): Boolean {
            val instance = permissionId.findCorrespondingPermissionOrFail()
            return INSTANCE.checkType(instance::class).testPermission(this@hasPermission, instance)
        }

        public fun Permittee.hasPermission(permissionId: PermissionId): Boolean =
            permissionId.testPermission(this@hasPermission)

        public fun Permittee.getPermittedPermissions(): Sequence<Permission> =
            INSTANCE.getPermittedPermissions(this@getPermittedPermissions.permitteeId)

        public fun Permittee.grantPermission(vararg permissions: Permission) {
            for (permission in permissions) {
                INSTANCE.checkType(permission::class).permit(this.permitteeId, permission)
            }
        }

        public fun Permittee.denyPermission(vararg permissions: Permission, recursive: Boolean) {
            for (permission in permissions) {
                INSTANCE.checkType(permission::class).cancel(this.permitteeId, permission, recursive)
            }
        }

        public fun PermitteeId.getPermittedPermissions(): Sequence<Permission> =
            INSTANCE.getPermittedPermissions(this@getPermittedPermissions)

        public fun Permission.testPermission(permittee: Permittee): Boolean =
            INSTANCE.checkType(this::class).testPermission(permittee.permitteeId, this@testPermission)

        public fun Permission.testPermission(permitteeId: PermitteeId): Boolean =
            INSTANCE.checkType(this::class).testPermission(permitteeId, this@testPermission)

        public fun PermissionId.testPermission(permittee: Permittee): Boolean {
            val p = INSTANCE[this] ?: return false
            return p.testPermission(permittee)
        }

        public fun PermissionId.testPermission(permissible: PermitteeId): Boolean {
            val p = INSTANCE[this] ?: return false
            return p.testPermission(permissible)
        }
    }
}