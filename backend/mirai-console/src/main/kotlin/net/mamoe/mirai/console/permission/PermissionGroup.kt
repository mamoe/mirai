/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.command.CommandSender
import kotlin.reflect.KProperty

@ExperimentalPermission
public abstract class PermissionGroup(
    private val identifierNamespace: PermissionIdentifierNamespace,
) {
    @ExperimentalPermission
    public inner class PermissionBuilder {
        @JvmField
        internal var description: String = "<no description given>"

        @JvmField
        internal var basePermission: PermissionIdentifier? = null

        @JvmField
        internal var permissionChecker: PermissionChecker? = null

        public fun description(description: String): PermissionBuilder = apply { this.description = description }
        public fun dependsOn(basePermission: PermissionIdentifier?): PermissionBuilder =
            apply { this.basePermission = basePermission }

        public fun dependsOn(basePermission: Permission?): PermissionBuilder =
            apply { this.basePermission = basePermission?.identifier }

        public fun basePermission(basePermission: PermissionIdentifier?): PermissionBuilder =
            apply { this.basePermission = basePermission }

        public fun basePermission(basePermission: Permission?): PermissionBuilder =
            apply { this.basePermission = basePermission?.identifier }

        public fun defaults(permissionChecker: PermissionChecker?): PermissionBuilder =
            apply { this.permissionChecker = permissionChecker }

        public fun build(property: KProperty<*>): Permission {
            return PermissionService.register(
                identifierNamespace.permissionIdentifier(property.name),
                description,
                basePermission
            )
        }
    }

    public infix fun String.dependsOn(permission: Permission): PermissionBuilder {
        return PermissionBuilder().apply { description(this@dependsOn);dependsOn(permission) }
    }


    public infix fun PermissionBuilder.defaults(permission: PermissionChecker): PermissionBuilder {
        return apply { defaults(permission) }
    }

    public infix fun PermissionBuilder.defaults(permission: CommandSender.() -> Boolean): PermissionBuilder {
        return apply { defaults(permission) }
    }

    public infix fun String.defaults(permission: PermissionChecker): PermissionBuilder {
        return PermissionBuilder().apply { defaults(permission) }
    }


    public operator fun String.invoke(block: PermissionBuilder.() -> Unit): PermissionBuilder {
        return PermissionBuilder().apply(block)
    }

    public operator fun String.provideDelegate(thisRef: PermissionGroup, property: KProperty<*>): Permission =
        PermissionBuilder().apply { description(this@provideDelegate) }.build(property)

    public operator fun Permission.getValue(thisRef: PermissionGroup, property: KProperty<*>): Permission = this
    public operator fun PermissionBuilder.getValue(thisRef: PermissionGroup, property: KProperty<*>): Permission =
        this.build(property)
}

