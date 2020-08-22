/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import kotlinx.serialization.SerialName
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Any> KType.asKClass(): KClass<out T> {
    val clazz = requireNotNull(classifier as? KClass<T>) { "Unsupported classifier: $classifier" }

    val fromClass = arguments[0].type?.classifier as? KClass<*> ?: Any::class
    val toClass = T::class

    require(toClass.isSubclassOf(fromClass)) {
        "Cannot cast KClass<${fromClass.qualifiedNameOrTip}> to KClass<${toClass.qualifiedNameOrTip}>"
    }

    return clazz
}

internal inline fun <reified T : PluginData> newPluginDataInstanceUsingReflection(type: KType): T {
    val classifier = type.asKClass<T>()

    return with(classifier) {
        objectInstance
            ?: createInstanceOrNull()
            ?: throw IllegalArgumentException(
                "Cannot create PluginData instance. " +
                        "PluginDataHolder supports PluginDatas implemented as an object " +
                        "or the ones with a constructor which either has no parameters or all parameters of which are optional, by default newPluginDataInstance implementation."
            )
    }
}


private fun isReferenceArray(rootClass: KClass<Any>): Boolean = rootClass.java.isArray

@Suppress("UNCHECKED_CAST")
private fun KType.kclass() = when (val t = classifier) {
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
internal fun KClass<*>.findASerialName(): String =
    findAnnotation<SerialName>()?.value
        ?: qualifiedName
        ?: throw IllegalArgumentException("Cannot find a serial name for $this")


internal val KProperty<*>.serialNameOrPropertyName: String get() = this.findAnnotation<SerialName>()?.value ?: this.name

internal fun Int.isOdd() = this and 0b1 != 0

internal val KProperty<*>.serialName: String get() = this.findAnnotation<SerialName>()?.value ?: this.name
