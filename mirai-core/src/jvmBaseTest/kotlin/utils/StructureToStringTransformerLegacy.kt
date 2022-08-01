/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.serialization.Transient
import net.mamoe.mirai.internal.testFramework.desensitizer.Desensitizer
import net.mamoe.mirai.utils.toUHexString
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaField

internal class StructureToStringTransformerLegacy : StructureToStringTransformer {
    override fun transform(any: Any?): String = any._miraiContentToString()
    override fun transformAndDesensitize(any: Any?): String = Desensitizer.desensitize(any._miraiContentToString())

    private val indent: String = " ".repeat(4)

    /**
     * 将所有元素加入转换为多行的字符串表示.
     */
    private fun <T> Sequence<T>.joinToStringPrefixed(prefix: String, transform: (T) -> CharSequence): String {
        return this.joinToString(prefix = "$prefix$indent", separator = "\n$prefix$indent", transform = transform)
    }

    /**
     * 将内容格式化为较可读的字符串输出.
     *
     * 各数字类型及其无符号类型: 十六进制表示 + 十进制表示. e.g. `0x1000(4096)`
     * [ByteArray] 和 [UByteArray]: 十六进制表示, 通过 [ByteArray.toUHexString]
     * [Iterable], [Iterator], [Sequence]: 调用各自的 joinToString.
     * [Map]: 多行输出. 每行显示一个值. 递归调用 [_miraiContentToString]. 嵌套结构将会以缩进表示
     * `data class`: 调用其 [toString]
     * 其他类型: 反射获取它和它的所有来自 Mirai 的 super 类型的所有自有属性并递归调用 [_miraiContentToString]. 嵌套结构将会以缩进表示
     */
    @Suppress("FunctionName") // 这样就不容易被 IDE 提示
    internal fun Any?._miraiContentToString(prefix: String = ""): String = when (this) {
        is Unit -> "Unit"
        is UInt -> "0x" + this.toUHexString("") + "($this)"
        is UByte -> "0x" + this.toUHexString() + "($this)"
        is UShort -> "0x" + this.toUHexString("") + "($this)"
        is ULong -> "0x" + this.toUHexString("") + "($this)"
        is Int -> "0x" + this.toUHexString("") + "($this)"
        is Byte -> "0x" + this.toUHexString() + "($this)"
        is Short -> "0x" + this.toUHexString("") + "($this)"
        is Long -> "0x" + this.toUHexString("") + "($this)"

        is Boolean -> if (this) "true" else "false"

        is ByteArray -> {
            if (this.size == 0) "<Empty ByteArray>"
            else this.toUHexString()
        }
        is UByteArray -> {
            if (this.size == 0) "<Empty UByteArray>"
            else this.toUHexString()
        }
        is ShortArray -> {
            if (this.size == 0) "<Empty ShortArray>"
            else this.iterator()._miraiContentToString()
        }
        is IntArray -> {
            if (this.size == 0) "<Empty IntArray>"
            else this.iterator()._miraiContentToString()
        }
        is LongArray -> {
            if (this.size == 0) "<Empty LongArray>"
            else this.iterator()._miraiContentToString()
        }
        is FloatArray -> {
            if (this.size == 0) "<Empty FloatArray>"
            else this.iterator()._miraiContentToString()
        }
        is DoubleArray -> {
            if (this.size == 0) "<Empty DoubleArray>"
            else this.iterator()._miraiContentToString()
        }
        is UShortArray -> {
            if (this.size == 0) "<Empty ShortArray>"
            else this.iterator()._miraiContentToString()
        }
        is UIntArray -> {
            if (this.size == 0) "<Empty IntArray>"
            else this.iterator()._miraiContentToString()
        }
        is ULongArray -> {
            if (this.size == 0) "<Empty LongArray>"
            else this.iterator()._miraiContentToString()
        }
        is Array<*> -> {
            if (this.size == 0) "<Empty Array>"
            else this.iterator()._miraiContentToString()
        }
        is BooleanArray -> {
            if (this.size == 0) "<Empty BooleanArray>"
            else this.iterator()._miraiContentToString()
        }

        is Iterable<*> -> this.joinToString(prefix = "[", postfix = "]") { it._miraiContentToString(prefix) }
        is Iterator<*> -> this.asSequence()
            .joinToString(prefix = "[", postfix = "]") { it._miraiContentToString(prefix) }
        is Sequence<*> -> this.joinToString(prefix = "[", postfix = "]") { it._miraiContentToString(prefix) }
        is Map<*, *> -> this.entries.joinToString(
            prefix = "{",
            postfix = "}"
        ) { it.key._miraiContentToString(prefix) + "=" + it.value._miraiContentToString(prefix) }
        else -> {
            if (this == null) "null"
            else if (this::class.isData) this.toString()
            else {
                if (this::class.qualifiedName?.startsWith("net.mamoe.mirai.") == true) {
                    this.contentToStringReflectively(prefix + indent)
                } else this.toString()
                /*
                (this::class.simpleName ?: "<UnnamedClass>") + "#" + this::class.hashCode() + "{\n" +
                        this::class.members.asSequence().filterIsInstance<KProperty<*>>().filter { !it.isSuspend && it.visibility == KVisibility.PUBLIC }
                            .joinToStringPrefixed(
                                prefix = indent
                            ) { it.name + "=" + kotlin.runCatching { it.call(it).contentToString(indent) }.getOrElse { "<!>" } }
                 */
            }
        }
    }

    internal fun KProperty1<*, *>.getValueAgainstPermission(receiver: Any): Any? {
        return this.javaField?.apply { isAccessible = true }?.get(receiver)
    }

    private fun Any.canBeIgnored(): Boolean {
        return when (this) {
            is String -> this.isEmpty()
            is ByteArray -> this.isEmpty()
            is Array<*> -> this.isEmpty()
            is Int -> this == 0
            is Float -> this == 0f
            is Double -> this == 0.0
            is Byte -> this == 0.toByte()
            is Short -> this == 0.toShort()
            is Long -> this == 0.toLong()
            else -> false
        }
    }

    private fun Any.contentToStringReflectively(
        prefix: String,
        filter: ((name: String, value: Any?) -> Boolean)? = null,
    ): String {
        val newPrefix = "$prefix    "
        return (this::class.simpleName ?: "<UnnamedClass>") + "#" + this::class.hashCode() + " {\n" +
                this.allMembersFromSuperClassesMatching { it.qualifiedName?.startsWith("net.mamoe.mirai") == true }
                    .distinctBy { it.name }
                    .filterNot { it.name.contains("$") || it.name == "Companion" || it.isConst || it.name == "serialVersionUID" }
                    .mapNotNull {
                        val value = it.getValueAgainstPermission(this) ?: return@mapNotNull null
                        if (filter != null) {
                            if (!filter(it.name, value))
                                return@mapNotNull it.name to value
                            else {
                                return@mapNotNull null
                            }
                        }
                        it.name to value
                    }
                    .joinToStringPrefixed(
                        prefix = newPrefix
                    ) { (name: String, value: Any?) ->
                        if (value.canBeIgnored()) ""
                        else {
                            "$name=" + kotlin.runCatching {
                                if (value == this) "<this>"
                                else value._miraiContentToString(newPrefix)
                            }.getOrElse { "<!>" }
                        }
                    }.lines().filterNot { it.isBlank() }.joinToString("\n") + "\n$prefix}"
    }

    private fun KClass<out Any>.thisClassAndSuperclassSequence(): Sequence<KClass<out Any>> {
        return sequenceOf(this) +
                this.supertypes.asSequence()
                    .mapNotNull { type ->
                        type.classifier?.takeIf { it is KClass<*> }?.takeIf { it != Any::class } as? KClass<out Any>
                    }.flatMap { it.thisClassAndSuperclassSequence() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Any.allMembersFromSuperClassesMatching(classFilter: (KClass<out Any>) -> Boolean): Sequence<KProperty1<Any, *>> {
        return this::class.thisClassAndSuperclassSequence()
            .filter { classFilter(it) }
            .map { it.members }
            .flatMap { it.asSequence() }
            .filterIsInstance<KProperty1<*, *>>()
            .filterNot { it.hasAnnotation<Transient>() }
            .filterNot { it.isTransient() }
            .map { it as KProperty1<Any, *> }
    }

    internal fun KProperty<*>.isTransient(): Boolean =
        javaField?.modifiers?.and(Modifier.TRANSIENT) != 0


}