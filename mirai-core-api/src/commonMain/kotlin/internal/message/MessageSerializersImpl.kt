/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.polymorphic
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.*
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSubclassOf

@MiraiInternalApi
public open class MessageSourceSerializerImpl(serialName: String) :
    KSerializer<MessageSource> by SerialData.serializer().map(
        resultantDescriptor = buildClassSerialDescriptor(serialName) {
            takeElementsFrom(SerialData.serializer().descriptor)
        },
        serialize = {
            SerialData(kind, botId, ids, internalIds, time, fromId, targetId, originalMessage)
        },
        deserialize = {
            Mirai.constructMessageSource(botId, kind, fromId, targetId, ids, time, internalIds, originalMessage)
        }
    ) {

    @MiraiInternalApi
    public companion object {
        public fun serialDataSerializer(): KSerializer<*> = SerialData.serializer()
    }

    @SerialName(MessageSource.SERIAL_NAME)
    @Serializable
    internal class SerialData(
        val kind: MessageSourceKind,
        val botId: Long,
        val ids: IntArray,
        val internalIds: IntArray,
        val time: Int,
        val fromId: Long,
        val targetId: Long,
        val originalMessage: MessageChain,
    )
}


// Tests:
// net.mamoe.mirai.internal.message.data.MessageSerializationTest
@OptIn(MiraiExperimentalApi::class)
internal object MessageSerializersImpl : MessageSerializers {
    private var serializersModuleField: SerializersModule by lateinitMutableProperty {
        SerializersModule { }
    }

    override val serializersModule: SerializersModule
        get() {
            Mirai // ensure registered, for tests
            return serializersModuleField
        }

    @Synchronized
    override fun <M : SingleMessage> registerSerializer(type: KClass<M>, serializer: KSerializer<M>) {
        serializersModuleField = serializersModule.overwritePolymorphicWith(type, serializer)
    }

    @Synchronized
    override fun registerSerializers(serializersModule: SerializersModule) {
        serializersModuleField = this.serializersModule.overwriteWith(serializersModule)
    }
}

internal fun <M : Any> SerializersModule.overwritePolymorphicWith(
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

//private inline fun <reified M : SingleMessage> SerializersModuleBuilder.hierarchicallyPolymorphic(serializer: KSerializer<M>) =
//    hierarchicallyPolymorphic(M::class, serializer)
//
//private fun <M : SingleMessage> SerializersModuleBuilder.hierarchicallyPolymorphic(
//    type: KClass<M>,
//    serializer: KSerializer<M>
//) {
//    // contextual(type, serializer)
//    for (superclass in type.allSuperclasses) {
//        if (superclass.isFinal) continue
//        if (!superclass.isSubclassOf(SingleMessage::class)) continue
//        @Suppress("UNCHECKED_CAST")
//        polymorphic(superclass as KClass<Any>) {
//            subclass(type, serializer)
//        }
//    }
//}