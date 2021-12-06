/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.compiler.common.diagnostics

import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_DECLARATION_RECEIVER
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_REGISTER_USE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_ID
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_NAME
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_NAMESPACE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_REGISTER_USE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_VERSION_REQUIREMENT
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.NOT_CONSTRUCTABLE_TYPE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.READ_ONLY_VALUE_CANNOT_BE_VAR
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.UNSERIALIZABLE_TYPE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.USING_DERIVED_CONCURRENT_MAP_TYPE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.USING_DERIVED_LIST_TYPE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.USING_DERIVED_MAP_TYPE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.USING_DERIVED_MUTABLE_LIST_TYPE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.USING_DERIVED_MUTABLE_MAP_TYPE
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

/**
 * @see MiraiConsoleErrors
 */
object MiraiConsoleErrorsRendering : DefaultErrorMessages.Extension {
    private val MAP = DiagnosticFactoryToRendererMap("MiraiConsole").apply {
        put(
            ILLEGAL_PLUGIN_DESCRIPTION,
            "{0}",
            Renderers.STRING,
        )

        put(
            NOT_CONSTRUCTABLE_TYPE,
            "类型 ''{1}'' 无法通过反射直接构造, 需要提供默认值.",
            Renderers.EMPTY,
            Renderers.STRING,
        )

        put(
            UNSERIALIZABLE_TYPE,
            "类型 ''{0}'' 无法被自动序列化, 需要添加序列化器",
            Renderers.FQ_NAMES_IN_TYPES,
        )

        put(
            ILLEGAL_COMMAND_NAME,
            "指令名 ''{0}'' 无效: {1}",
            Renderers.STRING,
            Renderers.STRING,
        )

        put(
            ILLEGAL_PERMISSION_NAME,
            "权限名 ''{0}'' 无效: {1}",
            Renderers.STRING,
            Renderers.STRING,
        )

        put(
            ILLEGAL_PERMISSION_ID,
            "权限 Id ''{0}'' 无效: {1}",
            Renderers.STRING,
            Renderers.STRING,
        )

        put(
            ILLEGAL_PERMISSION_NAMESPACE,
            "权限命名空间 ''{0}'' 无效: {1}",
            Renderers.STRING,
            Renderers.STRING,
        )

        put(
            ILLEGAL_COMMAND_REGISTER_USE,
            "''{0}'' 无法使用在 ''{1}'' 环境下.",
            Renderers.DECLARATION_NAME,
            Renderers.STRING
        )

        put(
            ILLEGAL_PERMISSION_REGISTER_USE,
            "''{0}'' 无法使用在 ''{1}'' 环境下.",
            Renderers.DECLARATION_NAME,
            Renderers.STRING
        )

        put(
            ILLEGAL_VERSION_REQUIREMENT,
            "{1}",
            Renderers.STRING,
            Renderers.STRING
        )

        put(
            ILLEGAL_COMMAND_DECLARATION_RECEIVER,
            "指令函数的接收者参数必须为 CommandSender 及其子类或无接收者.",
        )

        put(
            RESTRICTED_CONSOLE_COMMAND_OWNER,
            "插件不允许使用 ConsoleCommandOwner 构造指令, 请使用插件主类作为 CommandOwner",
        )

        put(
            READ_ONLY_VALUE_CANNOT_BE_VAR,
            "在 ReadOnlyPluginData 中不可定义 'var' by value",
        )

        put(
            USING_DERIVED_MAP_TYPE,
            "使用 'Map' 的派生类型 {1}.",
            Renderers.EMPTY,
            Renderers.STRING,
        )

        put(
            USING_DERIVED_MUTABLE_MAP_TYPE,
            "使用 'MutableMap' 的派生类型 {1}.",
            Renderers.EMPTY,
            Renderers.STRING,
        )

        put(
            USING_DERIVED_LIST_TYPE,
            "使用 'List' 的派生类型 {1}.",
            Renderers.EMPTY,
            Renderers.STRING,
        )

        put(
            USING_DERIVED_MUTABLE_LIST_TYPE,
            "使用 'MutableList' 的派生类型 {1}.",
            Renderers.EMPTY,
            Renderers.STRING,
        )

        put(
            USING_DERIVED_CONCURRENT_MAP_TYPE,
            "使用 'ConcurrentMap' 的派生类型 {1}.",
            Renderers.EMPTY,
            Renderers.STRING,
        )

//        put(
//            INAPPLICABLE_COMMAND_ANNOTATION,
//            "''{0}'' 无法在顶层函数使用.",
//            Renderers.STRING,
//        )
    }

    override fun getMap() = MAP
}
