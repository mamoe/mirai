/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.util.safeCast
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@ExperimentalCommandDescriptors
public interface CommandDescriptor {
    public val overloads: List<CommandSignatureVariant>
}

@ExperimentalCommandDescriptors
public interface CommandSignatureVariant {
    public val valueParameters: List<CommandValueParameter<*>>
}


/**
 * Inherited instances must be [CommandValueParameter]
 */
@ExperimentalCommandDescriptors
public interface ICommandParameter<T : Any?> {
    public val name: String
    public val type: KType
    public val defaultValue: T?

    public companion object {
        @get:JvmStatic
        @ExperimentalCommandDescriptors
        public val ICommandParameter<*>.isOptional: Boolean
            get() = this.defaultValue != null

    }
}

@ExperimentalCommandDescriptors
public sealed class CommandValueParameter<T> : ICommandParameter<T> {
    init {
        @Suppress("LeakingThis")
        require(type.classifier?.safeCast<KClass<*>>()?.isInstance(defaultValue) == true) {
            "defaultValue is not instance of type"
        }
    }

    public class StringConstant(
        public override val name: String,
        public override val defaultValue: String,
    ) : CommandValueParameter<String>() {
        override val type: KType get() = STRING_TYPE

        private companion object {
            @OptIn(ExperimentalStdlibApi::class)
            val STRING_TYPE = typeOf<String>()
        }
    }

    /**
     * Extended by [CommandArgumentParser]
     */
    public abstract class Extended<T> : CommandValueParameter<T>()
}