/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.polymorphic
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSubclassOf

internal actual fun <M : Any> SerializersModule.overwritePolymorphicWith(
    type: KClass<M>,
    serializer: KSerializer<M>
): SerializersModule {
    return overwriteWith(SerializersModule {
        // contextual(type, serializer)
        for (superclass in type.allSuperclasses) {
            if (superclass.isFinal) continue
            if (!superclass.isSubclassOf(SingleMessage::class)) continue
            @Suppress("UNCHECKED_CAST")
            polymorphic(superclass as KClass<Any>) {
                subclass(type, serializer)
            }
        }
    })
}
