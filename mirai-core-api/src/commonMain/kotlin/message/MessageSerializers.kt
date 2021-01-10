/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import net.mamoe.mirai.internal.message.MessageSerializersImpl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.reflect.KClass

/**
 * 消息序列化器.
 *
 * [MessageSerializers] 存放 [SerializersModule], 用于协助 [SingleMessage.Serializer] 的多态序列化.
 *
 * 要序列化一个 [MessageChain], 请使用内建的 [MessageChain.serializeToJsonString]
 *
 * @see serializersModule
 *
 *
 * @see SingleMessage.Serializer
 * @see MessageChain.Serializer
 *
 * @see MessageSerializers.INSTANCE
 */
public interface MessageSerializers {
    /**
     * 包含 [SingleMessage] 多态序列化和 [Message] [ContextualSerializer] 信息的 [SerializersModule].
     *
     * 在序列化消息时都需要提供给相关 [Json] 配置的 [Json.serializersModule]. 如通过:
     * ```
     * val json = Json {
     *     serializesModule = MessageSerializers.serializersModule
     * }
     * ```
     */
    public val serializersModule: SerializersModule

    /**
     * 注册一个 [SerializersModuleBuilder.contextual] 和 [SingleMessage] 多态域的 [PolymorphicModuleBuilder.subclass].
     *
     * 相当于
     * ```
     * contextual(baseClass, serializer)
     * polymorphic(SingleMessage::class) {
     *     subclass(baseClass, serializer)
     * }
     * ```
     *
     *
     * 若要自己实现消息类型, 务必在这里注册对应序列化器, 否则在 [MessageChain.serializeToJsonString] 时将会出错.
     */
    @MiraiExperimentalApi
    public fun <M : SingleMessage> registerSerializer(baseClass: KClass<M>, serializer: KSerializer<M>)

    /**
     * 合并 [serializersModule] 到 [MessageSerializers.serializersModule] 并覆盖.
     */
    @MiraiExperimentalApi
    public fun registerSerializers(serializersModule: SerializersModule)

    public companion object INSTANCE : MessageSerializers by MessageSerializersImpl
}