package net.mamoe.mirai.utils.cryptor

import java.lang.reflect.Field
import kotlin.reflect.full.allSuperclasses


val FIELD_TRY_SET_ACCESSIBLE = Field::class.java.declaredMethods.firstOrNull { it.name == "trySetAccessible" }

actual fun Any.contentToStringReflectively(prefix: String, filter: ((name: String, value: Any?) -> Boolean)?): String {
    val newPrefix = prefix + ProtoMap.indent
    return (this::class.simpleName ?: "<UnnamedClass>") + "#" + this::class.hashCode() + " {\n" +
            this.allFieldsFromSuperClassesMatching { it.name.startsWith("net.mamoe.mirai") }
                .distinctBy { it.name }
                .filterNot { it.name.contains("$") || it.name == "Companion" || it.isSynthetic || it.name == "serialVersionUID" }
                .filterNot { it.isEnumConstant }
                .map {
                    FIELD_TRY_SET_ACCESSIBLE?.invoke(it, true) ?: kotlin.run { it.isAccessible = true }
                    val value = it.get(this)
                    if (filter != null) {
                        kotlin.runCatching {
                            if (!filter(it.name, value)) return@map it.name to FIELD_TRY_SET_ACCESSIBLE
                        }
                    }
                    it.name to value
                }
                .filterNot { it.second === FIELD_TRY_SET_ACCESSIBLE }
                .joinToStringPrefixed(
                    prefix = newPrefix
                ) { (name, value) ->
                    "$name=" + kotlin.runCatching {
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