/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.compiler.common.diagnostics

import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_REGISTER_USE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_ID
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_NAME
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_NAMESPACE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_REGISTER_USE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.NOT_CONSTRUCTABLE_TYPE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.UNSERIALIZABLE_TYPE
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

object MiraiConsoleErrorsRendering : DefaultErrorMessages.Extension {
    private val MAP = DiagnosticFactoryToRendererMap("MiraiConsole").apply {
        put(
            ILLEGAL_PLUGIN_DESCRIPTION,
            "{0}",
            Renderers.STRING,
        )

        put(
            NOT_CONSTRUCTABLE_TYPE,
            "类型 ''{0}'' 无法通过反射直接构造, 需要提供默认值.",
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
    }

    override fun getMap() = MAP
}
