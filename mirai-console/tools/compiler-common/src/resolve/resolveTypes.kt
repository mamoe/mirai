/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.compiler.common.resolve

import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.firstValue
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.EnumValue

///////////////////////////////////////////////////////////////////////////
// OTHERS
///////////////////////////////////////////////////////////////////////////

val SERIALIZABLE_FQ_NAME = FqName("kotlinx.serialization.Serializable")
val AUTO_SERVICE = FqName("com.google.auto.service.AutoService")


///////////////////////////////////////////////////////////////////////////
// Command
///////////////////////////////////////////////////////////////////////////

val COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME = FqName("net.mamoe.mirai.console.command.CompositeCommand.SubCommand")
val SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME = FqName("net.mamoe.mirai.console.command.SimpleCommand.Handler")
val COMMAND_SENDER_FQ_NAME = FqName("net.mamoe.mirai.console.command.CommandSender")
val CONSOLE_COMMAND_SENDER_FQ_NAME = FqName("net.mamoe.mirai.console.command.ConsoleCommandSender")
val CONSOLE_COMMAND_OWNER_FQ_NAME = FqName("net.mamoe.mirai.console.command.ConsoleCommandOwner")

///////////////////////////////////////////////////////////////////////////
// Plugin
///////////////////////////////////////////////////////////////////////////

val PLUGIN_FQ_NAME = FqName("net.mamoe.mirai.console.plugin.Plugin")
val JVM_PLUGIN_DESCRIPTION_FQ_NAME = FqName("net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription")
val SIMPLE_JVM_PLUGIN_DESCRIPTION_FQ_NAME = FqName("net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription")

///////////////////////////////////////////////////////////////////////////
// PluginData
///////////////////////////////////////////////////////////////////////////

val PLUGIN_DATA_VALUE_FUNCTIONS_FQ_FQ_NAME = FqName("net.mamoe.mirai.console.data.value")
val READ_ONLY_PLUGIN_DATA_FQ_NAME = FqName("net.mamoe.mirai.console.data.ReadOnlyPluginData")

///////////////////////////////////////////////////////////////////////////
// Resolve
///////////////////////////////////////////////////////////////////////////

val RESOLVE_CONTEXT_FQ_NAME = FqName("net.mamoe.mirai.console.compiler.common.ResolveContext")

/**
 * net.mamoe.mirai.console.compiler.common.ResolveContext.Kind
 */
typealias ResolveContextKind = ResolveContext.Kind

fun ResolveContext.Kind.Companion.valueOfOrNull(string: String) =
    ResolveContext.Kind.values().find { it.name == string }

val Annotated.resolveContextKinds: List<ResolveContextKind>?
    get() {
        val ann = this.findAnnotation(RESOLVE_CONTEXT_FQ_NAME) ?: return null

        // https://github.com/mamoe/mirai-console/issues/363
        if (ann.allValueArguments.isEmpty()) return null

        return ann.allValueArguments
            .firstValue()
            .castOrNull<ArrayValue>()?.value
            ?.mapNotNull { value ->
                val (_, enumEntryName) = value.castOrNull<EnumValue>()?.value ?: return@mapNotNull null
                ResolveContextKind.valueOfOrNull(enumEntryName.asString())
            }
            ?: return null // undetermined kind
    }