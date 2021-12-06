/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.internal.permission.checkType
import net.mamoe.mirai.console.permission.Permission.Companion.parentsWithSelf
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.reflect.KClass

/**
 * 权限服务. 用于承载 Console 的权限系统.
 *
 * ### 可扩展
 * 权限服务可由插件扩展并覆盖默认实现.
 *
 * @see PermissionServiceProvider 相应扩展
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
     *
     * 备注: Java 实现者使用 `CollectionsKt.asSequence(Collection)` 构造 [Sequence]
     */
    public fun getRegisteredPermissions(): Sequence<P>

    /**
     * 获取 [PermitteeId] 和其父标识的所有被授予的所有直接和间接的权限列表
     *
     * 备注: Java 实现者使用 `CollectionsKt.asSequence(Collection)` 构造 [Sequence]
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
     *
     * @param description 描述. 将会展示给用户.
     *
     * @return 申请到的 [Permission] 实例
     *
     * @see get 获取一个已注册的权限
     * @see getOrFail 获取一个已注册的权限
     */
    @Throws(PermissionRegistryConflictException::class)
    public fun register(
        id: PermissionId,
        description: String,
        parent: Permission = RootPermission,
    ): P

    /** 为 [Plugin] 分配一个 [PermissionId] */
    @ConsoleExperimentalApi
    public fun allocatePermissionIdForPlugin(
        plugin: Plugin,
        @ResolveContext(COMMAND_NAME) permissionName: String,
    ): PermissionId = PermissionId(
        plugin.description.id.lowercase(),
        permissionName.lowercase()
    )

    ///////////////////////////////////////////////////////////////////////////

    /**
     * 授予 [permitteeId] 以 [permission] 权限
     *
     * Console 内建的权限服务支持此操作. 但插件扩展的权限服务可能不支持.
     *
     * @throws UnsupportedOperationException 当插件扩展的 [PermissionService] 不支持这样的操作时抛出.
     */
    @Throws(UnsupportedOperationException::class)
    public fun permit(permitteeId: PermitteeId, permission: P)

    /**
     * 撤销 [permitteeId] 的 [permission] 授权
     *
     * Console 内建的权限服务支持此操作. 但插件扩展的权限服务可能不支持.
     *
     * @param recursive `true` 时递归撤销所有子权限.
     * 例如, 若 [permission] 为 "*:*",
     * recursive 为 `true` 时撤销全部权限 (因为所有权限都是 "*:*" 的子权限);
     * 而为 `false` 时仅撤销 "*:*" 本身, 而不会影响子权限.
     *
     * @throws UnsupportedOperationException 当插件扩展的 [PermissionService] 不支持这样的操作时抛出.
     */
    @Throws(UnsupportedOperationException::class)
    public fun cancel(permitteeId: PermitteeId, permission: P, recursive: Boolean)

    public companion object {
        /**
         * [PermissionService] 实例
         *
         * @see PermissionServiceProvider.selectedInstance
         */
        @get:JvmName("getInstance")
        @JvmStatic
        public val INSTANCE: PermissionService<out Permission>
            get() = PermissionServiceProvider.selectedInstance

        /**
         * 获取一个权限, 失败时抛出 [NoSuchElementException]
         *
         * @see register 申请并注册一个权限
         */
        @JvmStatic
        @Throws(NoSuchElementException::class)
        public fun <P : Permission> PermissionService<P>.getOrFail(id: PermissionId): P =
            get(id) ?: throw NoSuchElementException("Permission not found: $id")

        /**
         * @see findCorrespondingPermission
         */
        @JvmStatic
        public val PermissionId.correspondingPermission: Permission?
            get() = findCorrespondingPermission()

        /**
         * @see get
         */
        @JvmStatic
        public fun PermissionId.findCorrespondingPermission(): Permission? = INSTANCE[this]

        /**
         * @see getOrFail
         * @throws NoSuchElementException
         */
        @Throws(NoSuchElementException::class)
        @JvmStatic
        public fun PermissionId.findCorrespondingPermissionOrFail(): Permission = INSTANCE.getOrFail(this)

        /**
         * @see PermissionService.permit
         */
        @JvmStatic
        @JvmName("permit0") // clash, not JvmSynthetic to allow possible calls from Java.
        public fun PermitteeId.permit(permission: Permission) {
            INSTANCE.checkType(permission::class).permit(this, permission)
        }

        /**
         * @see PermissionService.permit
         * @throws NoSuchElementException
         */
        @JvmStatic
        @Throws(NoSuchElementException::class)
        public fun PermitteeId.permit(permissionId: PermissionId) {
            permit(permissionId.findCorrespondingPermissionOrFail())
        }

        /**
         * @see PermissionService.cancel
         */
        @JvmSynthetic
        @JvmStatic
        @JvmName("cancel0") // clash, not JvmSynthetic to allow possible calls from Java.
        public fun PermitteeId.cancel(permission: Permission, recursive: Boolean) {
            INSTANCE.checkType(permission::class).cancel(this, permission, recursive)
        }

        /**
         * @see PermissionService.cancel
         * @throws NoSuchElementException
         */
        @JvmStatic
        @Throws(NoSuchElementException::class)
        public fun PermitteeId.cancel(permissionId: PermissionId, recursive: Boolean) {
            cancel(permissionId.findCorrespondingPermissionOrFail(), recursive)
        }

        /**
         * @see PermissionService.testPermission
         */
        @JvmStatic
        public fun Permittee.hasPermission(permission: Permission): Boolean =
            permission.testPermission(this@hasPermission)

        /**
         * @see PermissionService.testPermission
         */
        @JvmStatic
        public fun PermitteeId.hasPermission(permission: Permission): Boolean =
            permission.testPermission(this@hasPermission)

        /**
         * @see PermissionService.testPermission
         * @throws NoSuchElementException
         */
        @JvmStatic
        @Throws(NoSuchElementException::class)
        public fun PermitteeId.hasPermission(permissionId: PermissionId): Boolean {
            val instance = permissionId.findCorrespondingPermissionOrFail()
            return INSTANCE.checkType(instance::class).testPermission(this@hasPermission, instance)
        }

        /**
         * @see PermissionService.testPermission
         */
        @JvmStatic
        public fun Permittee.hasPermission(permissionId: PermissionId): Boolean =
            permissionId.testPermission(this@hasPermission)


        /**
         * @see PermissionService.getPermittedPermissions
         */
        @JvmStatic
        public fun Permittee.getPermittedPermissions(): Sequence<Permission> =
            INSTANCE.getPermittedPermissions(this@getPermittedPermissions.permitteeId)


        /**
         * @see PermissionService.permit
         */
        @JvmStatic
        public fun Permittee.permit(vararg permissions: Permission) {
            for (permission in permissions) {
                INSTANCE.checkType(permission::class).permit(this.permitteeId, permission)
            }
        }

        /**
         * @see PermissionService.cancel
         */
        @JvmStatic
        public fun Permittee.cancel(vararg permissions: Permission, recursive: Boolean) {
            for (permission in permissions) {
                INSTANCE.checkType(permission::class).cancel(this.permitteeId, permission, recursive)
            }
        }

        /**
         * @see PermissionService.getPermittedPermissions
         */
        @JvmSynthetic
        @JvmStatic
        @JvmName("getPermittedPermissions0") // clash, not JvmSynthetic to allow possible calls from Java.
        public fun PermitteeId.getPermittedPermissions(): Sequence<Permission> =
            INSTANCE.getPermittedPermissions(this@getPermittedPermissions)

        /**
         * @see PermissionService.testPermission
         */
        @JvmStatic
        public fun Permission.testPermission(permittee: Permittee): Boolean =
            INSTANCE.checkType(this::class).testPermission(permittee.permitteeId, this@testPermission)

        /**
         * @see PermissionService.testPermission
         */
        @JvmStatic
        public fun Permission.testPermission(permitteeId: PermitteeId): Boolean =
            INSTANCE.checkType(this::class).testPermission(permitteeId, this@testPermission)

        /**
         * @see PermissionService.testPermission
         */
        @JvmStatic
        public fun PermissionId.testPermission(permittee: Permittee): Boolean {
            val p = INSTANCE[this] ?: return false
            return p.testPermission(permittee)
        }

        /**
         * @see PermissionService.testPermission
         */
        @JvmStatic
        public fun PermissionId.testPermission(permissible: PermitteeId): Boolean {
            val p = INSTANCE[this] ?: return false
            return p.testPermission(permissible)
        }
    }
}
