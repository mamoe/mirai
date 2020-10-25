/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.compiler.common

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.util.SemVersion
import kotlin.annotation.AnnotationTarget.*

/**
 * 标记一个参数的语境类型, 用于帮助编译器和 IntelliJ 插件进行语境推断.
 */
@Target(VALUE_PARAMETER, PROPERTY, FIELD, FUNCTION, TYPE, TYPE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
public annotation class ResolveContext(
    vararg val kinds: Kind,
) {
    /**
     * 元素数量可能在任意时间被改动
     */
    public enum class Kind {
        ///////////////////////////////////////////////////////////////////////////
        // ConstantKind
        ///////////////////////////////////////////////////////////////////////////

        /*
         * WARNING: IF YOU CHANGE NAMES HERE,
         * YOU SHOULD ALSO CHANGE THEIR COUNTERPARTS AT net.mamoe.mirai.console.compiler.common.resolve.ResolveContextKind
         */

        /**
         * @see PluginDescription.id
         */
        PLUGIN_ID, // ILLEGAL_PLUGIN_DESCRIPTION

        /**
         * @see PluginDescription.name
         */
        PLUGIN_NAME, // ILLEGAL_PLUGIN_DESCRIPTION

        /**
         * @see PluginDescription.version
         * @see SemVersion.Companion.invoke
         */
        SEMANTIC_VERSION, // ILLEGAL_PLUGIN_DESCRIPTION

        /**
         * @see SemVersion.Companion.parseRangeRequirement
         */
        VERSION_REQUIREMENT, // ILLEGAL_VERSION_REQUIREMENT // TODO

        /**
         * @see Command.allNames
         */
        COMMAND_NAME, // ILLEGAL_COMMAND_NAME

        /**
         * @see PermissionId.name
         */
        PERMISSION_NAMESPACE, // ILLEGAL_PERMISSION_NAMESPACE

        /**
         * @see PermissionId.name
         */
        PERMISSION_NAME, // ILLEGAL_PERMISSION_NAME

        /**
         * @see PermissionId.parseFromString
         */
        PERMISSION_ID, // ILLEGAL_PERMISSION_ID

        /**
         * 标注一个泛型, 要求这个泛型必须拥有一个公开无参 (或所有参数都可选) 构造器.
         *
         * @see PluginData.value
         */
        RESTRICTED_NO_ARG_CONSTRUCTOR, // NOT_CONSTRUCTABLE_TYPE
    }
}