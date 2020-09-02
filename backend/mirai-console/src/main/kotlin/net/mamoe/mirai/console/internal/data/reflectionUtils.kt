/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Any> KType.toKClass(): KClass<out T> {
    val clazz = requireNotNull(classifier as? KClass<T>) { "Unsupported classifier: $classifier" }

    val fromClass = arguments[0].type?.classifier as? KClass<*> ?: Any::class
    val toClass = T::class

    require(toClass.isSubclassOf(fromClass)) {
        "Cannot cast KClass<${fromClass.qualifiedNameOrTip}> to KClass<${toClass.qualifiedNameOrTip}>"
    }

    return clazz
}

internal inline fun <reified T : PluginData> newPluginDataInstanceUsingReflection(type: KType): T {
    val classifier = type.toKClass<T>()

    return with(classifier) {
        objectInstance
            ?: createInstanceOrNull()
            ?: throw IllegalArgumentException(
                "Cannot create PluginData instance. " +
                        "PluginDataHolder supports PluginData implemented as an object " +
                        "or the ones with a constructor which either has no parameters or all parameters of which are optional, by default newPluginDataInstance implementation."
            )
    }
}


@Suppress("UNCHECKED_CAST")
internal fun KType.classifierAsKClass() = when (val t = classifier) {
    is KClass<*> -> t
    else -> error("Only KClass supported as classifier, got $t")
} as KClass<Any>

@JvmSynthetic
internal fun <T : Any> KClass<T>.createInstanceOrNull(): T? {
    val noArgsConstructor = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) }
        ?: return null

    return noArgsConstructor.callBy(emptyMap())
}

@JvmSynthetic
internal fun KClass<*>.findValueName(): String =
    findAnnotation<ValueName>()?.value
        ?: qualifiedName
        ?: throw IllegalArgumentException("Cannot find a serial name for $this")


internal fun Int.isOdd() = this and 0b1 != 0

internal val KProperty<*>.valueName: String get() = this.findAnnotation<ValueName>()?.value ?: this.name

internal inline val Any.kClassQualifiedName: String? get() = this::class.qualifiedName
internal inline val Any.kClassQualifiedNameOrTip: String get() = this::class.qualifiedNameOrTip