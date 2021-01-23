/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.polymorphic
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageChainImpl
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.reflect.KClass


internal fun ClassSerialDescriptorBuilder.takeElementsFrom(descriptor: SerialDescriptor) {
    with(descriptor) {
        repeat(descriptor.elementsCount) { index ->
            element(
                elementName = getElementName(index),
                descriptor = getElementDescriptor(index),
                annotations = getElementAnnotations(index),
                isOptional = isElementOptional(index),
            )
        }
    }
}

@MiraiInternalApi
public open class MessageSourceSerializerImpl(serialName: String) :
    KSerializer<MessageSource> by SerialData.serializer().map(
        resultantDescriptor = buildClassSerialDescriptor(serialName) {
            takeElementsFrom(SerialData.serializer().descriptor)
        },
        serialize = {
            // TODO: 2021-01-09 解决因为 originMessage 中 MessageSource 与 this 相同造成的死循环
            SerialData(kind, botId, ids, internalIds, time, fromId, targetId, originalMessage)
        },
        deserialize = {
            Mirai.constructMessageSource(botId, kind, fromId, targetId, ids, time, internalIds, originalMessage)
        }
    ) {
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


private val builtInSerializersModule by lazy {
    SerializersModule {
        // non-Message classes
        contextual(RawForwardMessage::class, RawForwardMessage.serializer())
        contextual(ForwardMessage.Node::class, ForwardMessage.Node.serializer())
        contextual(VipFace.Kind::class, VipFace.Kind.serializer())


        // In case Proguard or something else obfuscated the Kotlin metadata, providing the serializers explicitly will help.
        contextual(At::class, At.serializer())
        contextual(AtAll::class, AtAll.serializer())
        contextual(CustomMessage::class, CustomMessage.serializer())
        contextual(CustomMessageMetadata::class, CustomMessageMetadata.serializer())
        contextual(Face::class, Face.serializer())
        contextual(Image::class, Image.Serializer)
        contextual(PlainText::class, PlainText.serializer())
        contextual(QuoteReply::class, QuoteReply.serializer())

        contextual(ForwardMessage::class, ForwardMessage.serializer())


        contextual(LightApp::class, LightApp.serializer())
        contextual(SimpleServiceMessage::class, SimpleServiceMessage.serializer())
        contextual(AbstractServiceMessage::class, AbstractServiceMessage.serializer())

        contextual(PttMessage::class, PttMessage.serializer())
        contextual(Voice::class, Voice.serializer())
        contextual(PokeMessage::class, PokeMessage.serializer())
        contextual(VipFace::class, VipFace.serializer())
        contextual(FlashImage::class, FlashImage.serializer())

        contextual(MusicShare::class, MusicShare.serializer())

        contextual(MessageSource::class, MessageSource.serializer())

        fun PolymorphicModuleBuilder<MessageMetadata>.messageMetadataSubclasses() {
            subclass(MessageSource::class, MessageSource.serializer())
            subclass(QuoteReply::class, QuoteReply.serializer())
        }

        fun PolymorphicModuleBuilder<MessageContent>.messageContentSubclasses() {
            subclass(At::class, At.serializer())
            subclass(AtAll::class, AtAll.serializer())
            subclass(Face::class, Face.serializer())
            subclass(Image::class, Image.Serializer)
            subclass(PlainText::class, PlainText.serializer())

            subclass(ForwardMessage::class, ForwardMessage.serializer())


            subclass(LightApp::class, LightApp.serializer())
            subclass(SimpleServiceMessage::class, SimpleServiceMessage.serializer())

            //  subclass(PttMessage::class, PttMessage.serializer())
            subclass(Voice::class, Voice.serializer())

            // subclass(HummerMessage::class, HummerMessage.serializer())
            subclass(PokeMessage::class, PokeMessage.serializer())
            subclass(VipFace::class, VipFace.serializer())
            subclass(FlashImage::class, FlashImage.serializer())

            subclass(MusicShare::class, MusicShare.serializer())
        }

        contextual(SingleMessage::class, SingleMessage.Serializer)
        contextual(MessageChain::class, MessageChain.Serializer)
        contextual(MessageChainImpl::class, MessageChainImpl.serializer())

//        polymorphicDefault(MessageChain::class) { MessageChainImpl.serializer() }

        //  polymorphic(SingleMessage::class) {
        //      subclass(MessageSource::class, MessageSource.serializer())
        //      default {
        //          Message.Serializer.serializersModule.getPolymorphic(Message::class, it)
        //      }
        //  }

        // polymorphic(Message::class) {
        //     subclass(PlainText::class, PlainText.serializer())
        // }
        polymorphic(SingleMessage::class) {
            messageContentSubclasses()
            messageMetadataSubclasses()
        }

        //contextual(SingleMessage::class, SingleMessage.Serializer)
        // polymorphic(SingleMessage::class, SingleMessage.Serializer) {
        //     messageContentSubclasses()
        //     messageMetadataSubclasses()
        //     singleMessageSubclasses()
        // }

        // contextual(MessageContent::class, MessageContent.Serializer)
        // polymorphic(MessageContent::class, MessageContent.Serializer) {
        //     messageContentSubclasses()
        // }

        // contextual(MessageMetadata::class, MessageMetadata.Serializer)
        // polymorphic(MessageMetadata::class, MessageMetadata.Serializer) {
        //     messageMetadataSubclasses()
        //
    }
}

// Tests:
// net.mamoe.mirai.internal.message.data.MessageSerializationTest
internal object MessageSerializersImpl : MessageSerializers {
    @Volatile
    private var serializersModuleField: SerializersModule? = null
    override val serializersModule: SerializersModule get() = serializersModuleField ?: builtInSerializersModule

    @Synchronized
    override fun <M : SingleMessage> registerSerializer(baseClass: KClass<M>, serializer: KSerializer<M>) {
        serializersModuleField = serializersModule.overwriteWith(SerializersModule {
            contextual(baseClass, serializer)
            polymorphic(SingleMessage::class) {
                subclass(baseClass, serializer)
            }
        })
    }

    @Synchronized
    override fun registerSerializers(serializersModule: SerializersModule) {
        serializersModuleField = serializersModule.overwriteWith(serializersModule)
    }
}

internal inline fun <T, R> KSerializer<T>.map(
    resultantDescriptor: SerialDescriptor,
    crossinline deserialize: T.(T) -> R,
    crossinline serialize: R.(R) -> T,
): KSerializer<R> {
    return object : KSerializer<R> {
        override val descriptor: SerialDescriptor get() = resultantDescriptor
        override fun deserialize(decoder: Decoder): R = this@map.deserialize(decoder).let { deserialize(it, it) }
        override fun serialize(encoder: Encoder, value: R) = serialize(encoder, value.let { serialize(it, it) })
    }
}

internal inline fun <T, R> KSerializer<T>.mapPrimitive(
    serialName: String,
    crossinline deserialize: (T) -> R,
    crossinline serialize: R.(R) -> T,
): KSerializer<R> {
    val kind = this@mapPrimitive.descriptor.kind
    check(kind is PrimitiveKind) { "kind must be PrimitiveKind but found $kind" }
    return object : KSerializer<R> {
        override fun deserialize(decoder: Decoder): R =
            this@mapPrimitive.deserialize(decoder).let(deserialize)

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, kind)
        override fun serialize(encoder: Encoder, value: R) =
            this@mapPrimitive.serialize(encoder, value.let { serialize(it, it) })
    }
}