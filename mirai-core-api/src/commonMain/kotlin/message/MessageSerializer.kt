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
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.serializersModuleOf
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.CombinedMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.reflect.KClass

@MiraiExperimentalApi
public interface MessageSerializer {
    public val serializersModule: SerializersModule

    public fun <M : Message> registerSerializer(clazz: KClass<M>, serializer: KSerializer<M>)

    public fun clearRegisteredSerializers()
}

internal object MessageSourceSerializer : KSerializer<MessageSource> {
    @Serializable
    class SerialData(
        val kind: MessageSourceKind,
        val bot: Long,
        val ids: IntArray,
        val internalIds: IntArray,
        val time: Int,
        val fromId: Long,
        val targetId: Long,
        val originalMessage: MessageChain,
    )

    override val descriptor: SerialDescriptor = SerialData.serializer().descriptor
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


private val builtInSerializersModule = SerializersModule {
    // In case Proguard or something else obfuscated the Kotlin metadata, providing the serializesrs explicity will help.
    contextual(At::class, At.serializer())
    contextual(AtAll::class, AtAll.serializer())
    contextual(CombinedMessage::class, CombinedMessage.serializer())
    contextual(CustomMessage::class, CustomMessage.serializer())
    contextual(CustomMessageMetadata::class, CustomMessageMetadata.serializer())
    contextual(Face::class, Face.serializer())
    contextual(MessageSource::class, MessageSource.serializer())
    contextual(Image::class, Image.Serializer)
    contextual(PlainText::class, PlainText.serializer())
    contextual(QuoteReply::class, QuoteReply.serializer())

    contextual(ForwardMessage::class, ForwardMessage.serializer())
    contextual(RawForwardMessage::class, RawForwardMessage.serializer())
    contextual(ForwardMessage.Node::class, ForwardMessage.Node.serializer())


    contextual(LightApp::class, LightApp.serializer())
    contextual(SimpleServiceMessage::class, SimpleServiceMessage.serializer())
    contextual(AbstractServiceMessage::class, AbstractServiceMessage.serializer())
    contextual(LongMessage::class, LongMessage.serializer())
    contextual(ForwardMessageInternal::class, ForwardMessageInternal.serializer())

    contextual(PttMessage::class, PttMessage.serializer())
    contextual(Voice::class, Voice.serializer())

    contextual(HummerMessage::class, HummerMessage.serializer())
    contextual(PokeMessage::class, PokeMessage.serializer())
    contextual(VipFace::class, VipFace.serializer())
    contextual(FlashImage::class, FlashImage.serializer())
    contextual(VipFace.Kind::class, VipFace.Kind.serializer())


    contextual(Message::class, Message.Serializer)
    contextual(MessageChain::class, MessageChain.Serializer)
}

internal object MessageSerializerImpl : MessageSerializer {
    override var serializersModule: SerializersModule = builtInSerializersModule

    @Synchronized
    override fun <M : Message> registerSerializer(clazz: KClass<M>, serializer: KSerializer<M>) {
        serializersModule = serializersModule.plus(serializersModuleOf(clazz, serializer))
    }

    @Synchronized
    override fun clearRegisteredSerializers() {
        serializersModule = builtInSerializersModule
    }
}