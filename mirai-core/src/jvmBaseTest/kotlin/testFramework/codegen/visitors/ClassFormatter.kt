/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.visitors

import net.mamoe.mirai.internal.testFramework.codegen.descriptors.ClassValueDesc

data class ClassFormatterContext(
    val desc: ClassValueDesc<*>,
    val visitor: ValueDescToStringRenderer,
    val rendererContext: RendererContext,
)

open class ClassFormatter {
    open fun formatClassName(context: ClassFormatterContext): String {
        val name = context.desc.type.qualifiedName ?: context.desc.type.simpleName ?: context.desc.type.toString()
        return wrapBacktickIfNecessary(name)
    }

    open fun formatClassProperty(context: ClassFormatterContext, propertyString: String?, valueString: String): String =
        "$propertyString = $valueString"

    companion object {
        protected fun wrapBacktickIfNecessary(name: String) = if (name.contains(' ') || name.contains('$')) {
            "`$name`"
        } else name
    }
}