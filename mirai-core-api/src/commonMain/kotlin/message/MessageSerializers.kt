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
import kotlinx.serialization.modules.*
import net.mamoe.mirai.internal.message.MessageSerializersImpl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.reflect.KClass

/**
 * 消息序列化器.
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
     * 在序列化消息时都需要
     */
    public val serializersModule: SerializersModule

    /**
     * 注册一个 [SerializersModuleBuilder.contextual] 和 [SingleMessage] 多态域的 [PolymorphicModuleBuilder.subclass]
     *
     * 相当于
     * ```
     * contextual(baseClass, serializer)
     * polymorphic(SingleMessage::class) {
     *     subclass(baseClass, serializer)
     * }
     * ```
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