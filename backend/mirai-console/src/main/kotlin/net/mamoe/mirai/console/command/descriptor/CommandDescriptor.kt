/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.command.descriptor.ArgumentAcceptance.Companion.isAcceptable
import net.mamoe.mirai.console.command.descriptor.CommandValueParameter.UserDefinedType.Companion.createOptional
import net.mamoe.mirai.console.command.descriptor.CommandValueParameter.UserDefinedType.Companion.createRequired
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.internal.data.classifierAsKClassOrNull
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.safeCast
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

/**
 * @see CommandSignatureVariantImpl
 */
@ExperimentalCommandDescriptors
public interface CommandSignatureVariant {
    public val valueParameters: List<CommandValueParameter<*>>

    public suspend fun call(resolvedCommandCall: ResolvedCommandCall)
}

@ExperimentalCommandDescriptors
public class CommandSignatureVariantImpl(
    override val valueParameters: List<CommandValueParameter<*>>,
    private val onCall: suspend CommandSignatureVariantImpl.(resolvedCommandCall: ResolvedCommandCall) -> Unit,
) : CommandSignatureVariant {
    override suspend fun call(resolvedCommandCall: ResolvedCommandCall) {
        return onCall(resolvedCommandCall)
    }
}


/**
 * Inherited instances must be [CommandValueParameter]
 */
@ExperimentalCommandDescriptors
public interface ICommandParameter<T : Any?> {
    public val name: String

    /**
     * If [isOptional] is `false`, [defaultValue] is always `null`.
     * Otherwise [defaultValue] may be `null` iff [T] is nullable.
     */
    public val defaultValue: T?
    public val isOptional: Boolean

    /**
     * Reified type of [T]
     */
    public val type: KType

    public val isVararg: Boolean

    public fun accepts(argument: CommandValueArgument, commandArgumentContext: CommandArgumentContext?): Boolean =
        accepting(argument, commandArgumentContext).isAcceptable

    public fun accepting(argument: CommandValueArgument, commandArgumentContext: CommandArgumentContext?): ArgumentAcceptance
}

@ExperimentalCommandDescriptors
public sealed class ArgumentAcceptance(
    /**
     * Higher means more acceptable
     */
    @ConsoleExperimentalApi
    public val acceptanceLevel: Int,
) {
    public object Direct : ArgumentAcceptance(Int.MAX_VALUE)

    public class WithTypeConversion(
        public val typeVariant: TypeVariant<*>,
    ) : ArgumentAcceptance(20)

    public class WithContextualConversion(
        public val parser: CommandValueArgumentParser<*>,
    ) : ArgumentAcceptance(10)

    public class ResolutionAmbiguity(
        public val candidates: List<TypeVariant<*>>,
    ) : ArgumentAcceptance(0)

    public object Impossible : ArgumentAcceptance(-1)

    public companion object {
        @JvmStatic
        public val ArgumentAcceptance.isAcceptable: Boolean
            get() = acceptanceLevel > 0

        @JvmStatic
        public val ArgumentAcceptance.isNotAcceptable: Boolean
            get() = acceptanceLevel <= 0
    }
}


@ExperimentalCommandDescriptors
public sealed class CommandValueParameter<T> : ICommandParameter<T> {
    internal fun validate() { // // TODO: 2020/10/18 net.mamoe.mirai.console.command.descriptor.CommandValueParameter.validate$mirai_console_mirai_console_main
        require(type.classifier?.safeCast<KClass<*>>()?.isInstance(defaultValue) == true) {
            "defaultValue is not instance of type"
        }
    }


    public override fun accepting(argument: CommandValueArgument, commandArgumentContext: CommandArgumentContext?): ArgumentAcceptance {
        val expectingType = this.type

        if (argument.type.isSubtypeOf(expectingType)) return ArgumentAcceptance.Direct

        argument.typeVariants.associateWith { typeVariant ->
            if (typeVariant.outType.isSubtypeOf(expectingType)) {
                // TODO: 2020/10/11 resolution ambiguity
                return ArgumentAcceptance.WithTypeConversion(typeVariant)
            }
        }
        expectingType.classifierAsKClassOrNull()?.let { commandArgumentContext?.get(it) }?.let { parser ->
            return ArgumentAcceptance.WithContextualConversion(parser)
        }
        return ArgumentAcceptance.Impossible
    }

    public class StringConstant(
        public override val name: String,
        public val expectingValue: String,
    ) : CommandValueParameter<String>() {
        public override val type: KType get() = STRING_TYPE
        public override val defaultValue: Nothing? get() = null
        public override val isOptional: Boolean get() = false
        public override val isVararg: Boolean get() = false

        private companion object {
            @OptIn(ExperimentalStdlibApi::class)
            val STRING_TYPE = typeOf<String>()
        }
    }

    /**
     * @see createOptional
     * @see createRequired
     */
    public class UserDefinedType<T>(
        public override val name: String,
        public override val defaultValue: T?,
        public override val isOptional: Boolean,
        public override val isVararg: Boolean,
        public override val type: KType,
    ) : CommandValueParameter<T>() {
        public companion object {
            @JvmStatic
            public inline fun <reified T : Any> createOptional(name: String, isVararg: Boolean, defaultValue: T): UserDefinedType<T> {
                @OptIn(ExperimentalStdlibApi::class)
                return UserDefinedType(name, defaultValue, true, isVararg, typeOf<T>())
            }

            @JvmStatic
            public inline fun <reified T : Any> createRequired(name: String, isVararg: Boolean): UserDefinedType<T> {
                @OptIn(ExperimentalStdlibApi::class)
                return UserDefinedType(name, null, false, isVararg, typeOf<T>())
            }
        }
    }

    /**
     * Extended by [CommandValueArgumentParser]
     */
    public abstract class Extended<T> : CommandValueParameter<T>()
}