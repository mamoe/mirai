package net.mamoe.mirai.utils.cryptor


actual fun Any.contentToStringReflectively(prefix: String): String {
    val newPrefix = prefix + ProtoMap.indent
    return (this::class.simpleName ?: "<UnnamedClass>") + "#" + this::class.hashCode() + " {\n" +
            this::class.java.fields.toMutableSet().apply { addAll(this::class.java.declaredFields) }.asSequence().filterNot { it.name.contains("$") || it.name == "Companion" || it.isSynthetic }
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