/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.serialization

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Collectd in [MessageProtocol.collectProcessors].
 * @see ProcessorCollector.add
 */
internal class MessageSerializer<T : Any>(
    /**
     * The exact class the [serializer] is for
     */
    val forClass: KClass<T>,
    val serializer: KSerializer<T>,
    /**
     * Polymorphic base
     */
    val superclasses: Array<out KClass<in T>>,
    /**
     * Register also this as contextual
     */
    val registerAlsoContextual: Boolean = superclasses.isEmpty(),
) {
    // This can help native targets, which has no reflection support.

    companion object {
        inline fun <T : SingleMessage, R> superclassesScope(
            vararg superclasses: KClass<in T>,
            block: SuperclassesScope<T>.() -> R
        ): R {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return block(SuperclassesScope(superclasses))
        }
    }
}

@JvmInline
internal value class SuperclassesScope<T : SingleMessage>(
    val superclasses: Array<out KClass<in T>>,
)

@Suppress("FunctionName")
internal fun <T : SingleMessage> SuperclassesScope<in T>.MessageSerializer(
    forClass: KClass<T>,
    serializer: KSerializer<T>,
    registerAlsoContextual: Boolean = true,
): MessageSerializer<T> = MessageSerializer(forClass, serializer, superclasses, registerAlsoContextual)