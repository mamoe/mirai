/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.permission

import net.mamoe.mirai.console.data.PluginDataExtensions
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.permission.Permission.Companion.parentsWithSelf
import net.mamoe.mirai.console.permission.PermitteeId.Companion.allParentsWithSelf
import net.mamoe.mirai.console.permission.PermitteeId.Companion.isChildOf

internal abstract class AbstractConcurrentPermissionService<P : Permission> : PermissionService<P> {
    protected abstract val permissions: MutableMap<PermissionId, P>
    protected abstract val grantedPermissionsMap: PluginDataExtensions.NotNullMutableMap<PermissionId, MutableCollection<PermitteeId>>

    protected abstract fun createPermission(id: PermissionId, description: String, parent: Permission): P

    override fun get(id: PermissionId): P? = permissions[id]

    override fun register(id: PermissionId, description: String, parent: Permission): P {
        val instance = createPermission(id, description, parent)
        val old = permissions.putIfAbsent(id, instance)
        if (old != null) throw PermissionRegistryConflictException(instance, old)
        return instance
    }

    override fun permit(permitteeId: PermitteeId, permission: P) {
        grantedPermissionsMap[permission.id].add(permitteeId)
    }

    override fun cancel(permitteeId: PermitteeId, permission: P, recursive: Boolean) {
        val success = if (recursive) {
            getPermittedPermissions(permitteeId).any { permitted ->
                (permission in permitted.parentsWithSelf) && grantedPermissionsMap[permitted.id].remove(permitteeId)
            }
        } else {
            grantedPermissionsMap[permission.id].remove(permitteeId)
        }
        if (!success) {
            val about = buildList {
                for ((permissionIdentifier, permissibleIdentifiers) in grantedPermissionsMap) {
                    val parent = get(permissionIdentifier) ?: continue
                    if (parent !in permission.parentsWithSelf) continue
                    for (permissibleId in permissibleIdentifiers) {
                        if (permissibleId in permitteeId.allParentsWithSelf) {
                            add(parent to permissibleId)
                        }
                    }
                }
            }
            val message = if (about.isEmpty()) {
                "${permitteeId.asString()} 不拥有权限 ${permission.id}"
            } else {
                buildString {
                    appendLine("${permitteeId.asString()} 的 ${permission.id} 权限来自")
                    about.forEach { (parent, permitted) ->
                        appendLine("${permitted.asString()} ${parent.id}")
                    }
                    appendLine("Mirai Console 内置权限系统目前不支持单独禁用继承得到的权限. 可取消继承来源再为其分别分配.")
                }
            }

            throw UnsupportedOperationException(message)
        }
    }

    override fun getRegisteredPermissions(): Sequence<P> = permissions.values.asSequence()

    override fun getPermittedPermissions(permitteeId: PermitteeId): Sequence<P> = sequence {
        for ((permissionIdentifier, permissibleIdentifiers) in grantedPermissionsMap) {

            val granted = permissibleIdentifiers.any { permitteeId.isChildOf(it) }

            if (granted) get(permissionIdentifier)?.let { yield(it) }
        }
    }

    internal fun getPermittedPermissionsAndSource(permitteeId: PermitteeId): Sequence<Pair<PermitteeId, P>> = sequence {
        for ((permissionIdentifier, permissibleIdentifiers) in grantedPermissionsMap) {
            permissibleIdentifiers.forEach { pid ->
                if (permitteeId.isChildOf(pid)) {
                    get(permissionIdentifier)?.let { yield(pid to it) }
                }
            }
        }
    }
}

internal fun PermitteeId.getPermittedPermissionsAndSource(): Sequence<Pair<PermitteeId, Permission>> {
    return when (val ps = PermissionService.INSTANCE) {
        is AbstractConcurrentPermissionService -> ps.getPermittedPermissionsAndSource(this)
        else -> ps.getPermittedPermissions(this).map { this to it }
    }
}
