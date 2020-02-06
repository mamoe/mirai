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