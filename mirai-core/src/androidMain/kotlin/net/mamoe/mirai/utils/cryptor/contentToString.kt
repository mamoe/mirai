/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.cryptor

import net.mamoe.mirai.utils.MiraiDebugAPI
import java.lang.reflect.Field
import kotlin.reflect.full.allSuperclasses


@MiraiDebugAPI
actual fun Any.contentToStringReflectively(prefix: String, filter: ((name: String, value: Any?) -> Boolean)?): String {
    return (this::class.simpleName ?: "<UnnamedClass>") + "#" + this::class.hashCode() + " {\n" +
            this.allFieldsFromSuperClassesMatching { it.name.startsWith("net.mamoe.mirai") }
                .distinctBy { it.name }
                .filterNot { it.name.contains("$") || it.name == "Companion" || it.isSynthetic || it.name == "serialVersionUID" }
                .joinToStringPrefixed(
                    prefix = prefix
                ) {
                    it.isAccessible = true
                    if (filter != null) {
                        kotlin.runCatching {
                            if (!filter(it.name, it.get(this))) return@joinToStringPrefixed ""
                        }
                    }
                    it.name + "=" + kotlin.runCatching {
                        val value = it.get(this)
                        if (value == this) "<this>"
                        else value.contentToString(prefix)
                    }.getOrElse { "<!>" }
                } + "\n$prefix}"
}

internal fun Any.allFieldsFromSuperClassesMatching(classFilter: (Class<out Any>) -> Boolean): Sequence<Field> {
    return (this::class.java.takeIf(classFilter)?.declaredFields?.asSequence() ?: sequenceOf<Field>()) + this::class.allSuperclasses
        .asSequence()
        .map { it.java }
        .filter(classFilter)
        .flatMap { it.declaredFields.asSequence() }
}