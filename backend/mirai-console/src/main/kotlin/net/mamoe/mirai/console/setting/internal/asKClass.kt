package net.mamoe.mirai.console.setting.internal

import net.mamoe.mirai.console.command.internal.qualifiedNameOrTip
import net.mamoe.mirai.console.setting.Setting
import kotlin.reflect.KClass
import kotlin.reflect.KType
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

internal inline fun <reified T : Setting> newSettingInstanceUsingReflection(type: KType): T {
    val classifier = type.asKClass<T>()

    return with(classifier) {
        objectInstance
            ?: createInstanceOrNull()
            ?: throw IllegalArgumentException(
                "Cannot create Setting instance. " +
                        "SettingHolder supports Settings implemented as an object " +
                        "or the ones with a constructor which either has no parameters or all parameters of which are optional, by default newSettingInstance implementation."
            )
    }
}
