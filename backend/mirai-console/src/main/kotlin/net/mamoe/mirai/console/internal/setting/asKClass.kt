/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.setting

import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
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
