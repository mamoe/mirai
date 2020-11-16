/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.resolve

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.command.descriptor.ArgumentAcceptance.Companion.isNotAcceptable
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.command.parse.DefaultCommandValueArgument
import net.mamoe.mirai.console.internal.data.classifierAsKClass
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.asMessageChain

/**
 * Builtin implementation of [CommandCallResolver]
 */
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public object BuiltInCommandCallResolver : CommandCallResolver {
    override fun resolve(call: CommandCall): CommandResolveResult {
        val callee = CommandManager.matchCommand(call.calleeName) ?: return CommandResolveResult(null)

        val valueArguments = call.valueArguments
        val context = callee.safeCast<CommandArgumentContextAware>()?.context

        val signature = resolveImpl(call.caller, callee, valueArguments, context) ?: return CommandResolveResult(null)

        return CommandResolveResult(ResolvedCommandCallImpl(call.caller,
            callee,
            signature.signature,
            signature.zippedArguments.map { it.second },
            context ?: EmptyCommandArgumentContext)
        )
    }

    private data class ResolveData(
        val signature: CommandSignature,
        val zippedArguments: List<Pair<AbstractCommandValueParameter<*>, CommandValueArgument>>,
        val argumentAcceptances: List<ArgumentAcceptanceWithIndex>,
        val remainingParameters: List<AbstractCommandValueParameter<*>>,
    ) {
        val remainingOptionalCount: Int = remainingParameters.count { it.isOptional }
    }

    private data class ArgumentAcceptanceWithIndex(
        val index: Int,
        val acceptance: ArgumentAcceptance,
    )

    private fun resolveImpl(
        caller: CommandSender,
        callee: Command,
        valueArguments: List<CommandValueArgument>,
        context: CommandArgumentContext?,
    ): ResolveData? {

        callee.overloads
            .mapNotNull l@{ signature ->
                if (signature.receiverParameter?.type?.classifierAsKClass()?.isInstance(caller) == false) {
                    return@l null // not compatible receiver
                }

                val valueParameters = signature.valueParameters

                val zipped = valueParameters.zip(valueArguments).toMutableList()

                val remainingParameters = valueParameters.drop(zipped.size).toMutableList()

                if (remainingParameters.any { !it.isOptional && !it.isVararg }) return@l null // not enough args. // vararg can be empty.

                if (zipped.isEmpty()) {
                    ResolveData(
                        signature = signature,
                        zippedArguments = emptyList(),
                        argumentAcceptances = emptyList(),
                        remainingParameters = remainingParameters,
                    )
                } else {
                    if (valueArguments.size > valueParameters.size && zipped.last().first.isVararg) {
                        // merge vararg arguments
                        val (varargParameter, _)
                            = zipped.removeLast()

                        zipped.add(varargParameter to DefaultCommandValueArgument(valueArguments.drop(zipped.size).map { it.value }.asMessageChain()))
                    } else {
                        // add default empty vararg argument
                        val remainingVararg = remainingParameters.find { it.isVararg }
                        if (remainingVararg != null) {
                            zipped.add(remainingVararg to DefaultCommandValueArgument(EmptyMessageChain))
                            remainingParameters.remove(remainingVararg)
                        }
                    }

                    ResolveData(
                        signature = signature,
                        zippedArguments = zipped,
                        argumentAcceptances = zipped.mapIndexed { index, (parameter, argument) ->
                            val accepting = parameter.accepting(argument, context)
                            if (accepting.isNotAcceptable) {
                                return@l null // argument type not assignable
                            }
                            ArgumentAcceptanceWithIndex(index, accepting)
                        },
                        remainingParameters = remainingParameters
                    )
                }
            }
            .also { result -> result.singleOrNull()?.let { return it } }
            .takeLongestMatches()
            .ifEmpty { return null }
            .also { result -> result.singleOrNull()?.let { return it } }
            // take single ArgumentAcceptance.Direct
            .also { list ->

                val candidates = list
                    .flatMap { phase ->
                        phase.argumentAcceptances.filter { it.acceptance is ArgumentAcceptance.Direct }.map { phase to it }
                    }
                candidates.singleOrNull()?.let { return it.first } // single Direct
                if (candidates.distinctBy { it.second.index }.size != candidates.size) {
                    // Resolution ambiguity
                    /*
                    open class A
                    open class AA: A()

                    open class C
                    open class CC: C()

                    fun foo(a: A, c: CC) = 1
                    fun foo(a: AA, c: C) = 1
                     */
                    // The call is foo(AA(), C()) or foo(A(), CC())
                    return null
                }
            }

        return null
    }

    /*


open class A
open class B : A()
open class C : A()
open class D : C()
open class BB : B()

fun foo(a: A, c: C) = 1
//fun foo(a: A, c: A) = 1
//fun foo(a: A, c: C, def: Int = 0) = 1
fun foo(a: B, c: C, d: D) = ""

fun foo(b: BB, a: A, d: C) = 1.0


fun main() {
    val a = foo(D(), D()) // int
    val b = foo(A(), C()) // int
    val d = foo(BB(), c = C(), D()) // string
}
     */

    private fun List<ResolveData>.takeLongestMatches(): Collection<ResolveData> {
        if (isEmpty()) return emptyList()
        return associateWith {
            it.signature.valueParameters.size - it.remainingOptionalCount * 1.001 // slightly lower priority with optional defaults.
        }.let { m ->
            val maxMatch = m.values.maxByOrNull { it }
            m.filter { it.value == maxMatch }.keys
        }
    }
}