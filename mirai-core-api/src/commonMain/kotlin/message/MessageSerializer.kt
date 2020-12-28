/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.reflect.KClass

@MiraiExperimentalApi
public interface MessageSerializer {
    public val serializersModule: SerializersModule

    public fun <M : Message> registerSerializer(clazz: KClass<M>, serializer: KSerializer<M>)

    public fun registerSerializers(serializersModule: SerializersModule)

    public fun clearRegisteredSerializers()
}

@MiraiInternalApi
public open class MessageSourceSerializerImpl(serialName: String) : KSerializer<MessageSource> {
    public companion object : MessageSourceSerializerImpl("net.mamoe.mirai.message.data.MessageSource")

    @Serializable
    internal class SerialData(
        val kind: MessageSourceKind,
        val bot: Long,
        val ids: IntArray,
        val internalIds: IntArray,
        val time: Int,
        val fromId: Long,
        val targetId: Long,
        val originalMessage: MessageChain,
    )

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName) {
        val desc = SerialData.serializer().descriptor
        repeat(SerialData.serializer().descriptor.elementsCount) { index ->
            element(
                desc.getElementName(index),
                desc.getElementDescriptor(index),
                desc.getElementAnnotations(index),
                desc.isElementOptional(index)
            )
        }
    }
//        buildClassSerialDescriptor("MessageSource") {
//            element("bot", Long.serializer().descriptor)
//            element("ids", ArraySerializer(Int.serializer()).descriptor)
//            element("internalIds", ArraySerializer(Int.serializer()).descriptor)
//            element("time", Int.serializer().descriptor)
//            element("fromId", Int.serializer().descriptor)
//            element("targetId", Int.serializer().descriptor)
//            element("originalMessage", MessageChain.Serializer.descriptor)
//        }

    override fun deserialize(decoder: Decoder): MessageSource {
        val data = SerialData.serializer().deserialize(decoder)
        data.run {
            return Mirai.constructMessageSource(
                botId = bot, kind = kind, fromUin = fromId, targetUin = targetId, ids = ids,
                time = time, internalIds = internalIds, originalMessage = originalMessage.asMessageChain()
            )
        }
    }

    override fun serialize(encoder: Encoder, value: MessageSource) {
        value.run {
            SerialData.serializer().serialize(
                encoder = encoder,
                value = SerialData(
                    kind = kind, bot = botId, ids = ids, internalIds = internalIds,
                    time = time, fromId = fromId, targetId = targetId, originalMessage = originalMessage
                )
            )
        }
    }
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
        contextual(LongMessage::class, LongMessage.serializer())
        contextual(ForwardMessageInternal::class, ForwardMessageInternal.serializer())

        contextual(PttMessage::class, PttMessage.serializer())
        contextual(Voice::class, Voice.serializer())
        contextual(PokeMessage::class, PokeMessage.serializer())
        contextual(VipFace::class, VipFace.serializer())
        contextual(FlashImage::class, FlashImage.serializer())

        fun PolymorphicModuleBuilder<SingleMessage>.singleMessageSubclasses() {
            // subclass(MessageSource::class, MessageSource.serializer())
        }

        //   contextual(MessageSource::class, MessageSource.serializer())
        polymorphicDefault(MessageSource::class) { MessageSource.serializer() }

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
            subclass(LongMessage::class, LongMessage.serializer())
            subclass(ForwardMessageInternal::class, ForwardMessageInternal.serializer())

            //  subclass(PttMessage::class, PttMessage.serializer())
            subclass(Voice::class, Voice.serializer())

            // subclass(HummerMessage::class, HummerMessage.serializer())
            subclass(PokeMessage::class, PokeMessage.serializer())
            subclass(VipFace::class, VipFace.serializer())
            subclass(FlashImage::class, FlashImage.serializer())
        }

        @Suppress("DEPRECATION_ERROR")
        contextual(Message::class, Message.Serializer)
        // contextual(SingleMessage::class, SingleMessage.Serializer)
        contextual(MessageChain::class, MessageChain.Serializer)
        contextual(MessageChainImpl::class, MessageChainImpl.serializer())

        polymorphic(MessageChain::class) {
            subclass(MessageChainImpl::class, MessageChainImpl.serializer())
        }
        polymorphicDefault(MessageChain::class) { MessageChainImpl.serializer() }

        polymorphic(AbstractServiceMessage::class) {
            subclass(LongMessage::class, LongMessage.serializer())
            subclass(ForwardMessageInternal::class, ForwardMessageInternal.serializer())
        }

        //  polymorphic(SingleMessage::class) {
        //      subclass(MessageSource::class, MessageSource.serializer())
        //      default {
        //          Message.Serializer.serializersModule.getPolymorphic(Message::class, it)
        //      }
        //  }

        polymorphicDefault(Image::class) { Image.Serializer }

        // polymorphic(Message::class) {
        //     subclass(PlainText::class, PlainText.serializer())
        // }
        polymorphic(Message::class) {
            messageContentSubclasses()
            messageMetadataSubclasses()
            singleMessageSubclasses()
            subclass(MessageChainImpl::class, MessageChainImpl.serializer())
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
        // }
    }
}

internal object MessageSerializerImpl : MessageSerializer {
    @Volatile
    private var serializersModuleField: SerializersModule? = null
    override val serializersModule: SerializersModule get() = serializersModuleField ?: builtInSerializersModule

    @Synchronized
    override fun <M : Message> registerSerializer(clazz: KClass<M>, serializer: KSerializer<M>) {
        serializersModuleField = serializersModule.plus(SerializersModule {
            contextual(clazz, serializer)
            polymorphic(Message::class) {
                subclass(clazz, serializer)
            }
        })
    }

    @Synchronized
    override fun registerSerializers(serializersModule: SerializersModule) {
        serializersModuleField = serializersModule
    }

    @Synchronized
    override fun clearRegisteredSerializers() {
        serializersModuleField = builtInSerializersModule
    }
}