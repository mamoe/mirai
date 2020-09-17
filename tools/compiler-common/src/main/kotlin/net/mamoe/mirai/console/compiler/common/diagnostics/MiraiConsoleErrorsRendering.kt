/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.compiler.common.diagnostics

import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PLUGIN_ID
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PLUGIN_NAME
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

object MiraiConsoleErrorsRendering : DefaultErrorMessages.Extension {
    private val MAP = DiagnosticFactoryToRendererMap("MiraiConsole").apply {
        put(
            ILLEGAL_PLUGIN_ID,
            "Illegal plugin id: '{0}'",
            Renderers.STRING
        )
        put(
            ILLEGAL_PLUGIN_NAME,
            "Illegal plugin name: '{0}'",
            Renderers.STRING
        )
    }

    override fun getMap() = MAP
}
