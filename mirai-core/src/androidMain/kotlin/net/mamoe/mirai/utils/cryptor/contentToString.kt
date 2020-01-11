package net.mamoe.mirai.utils.cryptor

import java.lang.reflect.Field
import kotlin.reflect.full.allSuperclasses


actual fun Any.contentToStringReflectively(prefix: String): String {
    val newPrefix = prefix + ProtoMap.indent
    return (this::class.simpleName ?: "<UnnamedClass>") + "#" + this::class.hashCode() + " {\n" +
            this.allFieldsFromSuperClassesMatching { it.name.startsWith("net.mamoe.mirai") }
                .distinctBy { it.name }
                .filterNot { it.name.contains("$") || it.name == "Companion" || it.isSynthetic || it.name == "serialVersionUID" }
                .joinToStringPrefixed(
                    prefix = newPrefix
                ) {
                    it.isAccessible = true
                    it.name + "=" + kotlin.runCatching {
                        val value = it.get(this)
                        if (value == this) "<this>"
                        else value.contentToString(newPrefix)
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