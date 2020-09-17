/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.resolve

import net.mamoe.mirai.console.intellij.line.marker.hasAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtNamedFunction

val COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME = FqName("net.mamoe.mirai.console.command.CompositeCommand.SubCommand")
val SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME = FqName("net.mamoe.mirai.console.command.SimpleCommand.Handler")

val Plugin_FQ_NAME = FqName("net.mamoe.mirai.console.plugin.Plugin")

/**
 * For CompositeCommand.SubCommand
 */
fun KtNamedFunction.isCompositeCommandSubCommand(): Boolean = this.hasAnnotation(COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME)

/**
 * SimpleCommand.Handler
 */
fun KtNamedFunction.isSimpleCommandHandler(): Boolean = this.hasAnnotation(SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME)

fun KtNamedFunction.isSimpleCommandHandlerOrCompositeCommandSubCommand(): Boolean =
    this.isSimpleCommandHandler() || this.isCompositeCommandSubCommand()