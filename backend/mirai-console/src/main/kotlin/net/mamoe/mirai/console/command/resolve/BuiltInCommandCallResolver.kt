package net.mamoe.mirai.console.command.resolve

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.command.descriptor.ArgumentAcceptance.Companion.isNotAcceptable
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.extensions.CommandCallResolverProvider
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.console.util.safeCast
import java.util.*

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public object BuiltInCommandCallResolver : CommandCallResolver {
    public object Provider : CommandCallResolverProvider(BuiltInCommandCallResolver)

    override fun resolve(call: CommandCall): ResolvedCommandCall? {
        val callee = CommandManager.matchCommand(call.calleeName) ?: return null

        val valueArguments = call.valueArguments
        val context = callee.safeCast<CommandArgumentContextAware>()?.context

        val signature = resolveImpl(callee, valueArguments, context) ?: return null

        return ResolvedCommandCallImpl(call.caller, callee, signature, call.valueArguments)
    }

    private data class ResolveData(
        val variant: CommandSignatureVariant,
        val argumentAcceptances: List<ArgumentAcceptanceWithIndex>,
    )

    private data class ArgumentAcceptanceWithIndex(
        val index: Int,
        val acceptance: ArgumentAcceptance,
    )

    private fun resolveImpl(
        callee: Command,
        valueArguments: List<CommandValueArgument>,
        context: CommandArgumentContext?,
    ): CommandSignatureVariant? {

        callee.overloads
            .mapNotNull l@{ signature ->
                val zipped = signature.valueParameters.zip(valueArguments)

                if (signature.valueParameters.drop(zipped.size).any { !it.isOptional }) return@l null // not enough args

                ResolveData(signature, zipped.mapIndexed { index, (parameter, argument) ->
                    val accepting = parameter.accepting(argument, context)
                    if (accepting.isNotAcceptable) {
                        return@l null // argument type not assignable
                    }
                    ArgumentAcceptanceWithIndex(index, accepting)
                })
            }
            .also { result -> result.singleOrNull()?.let { return it.variant } }
            .takeLongestMatches()
            .ifEmpty { return null }
            .also { result -> result.singleOrNull()?.let { return it.variant } }
            // take single ArgumentAcceptance.Direct
            .also { list ->

                val candidates = list
                    .flatMap { phase ->
                        phase.argumentAcceptances.filter { it.acceptance is ArgumentAcceptance.Direct }.map { phase to it }
                    }
                candidates.singleOrNull()?.let { return it.first.variant } // single Direct
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

    private fun List<ResolveData>.takeLongestMatches(): List<ResolveData> {
        if (isEmpty()) return emptyList()
        return associateByTo(TreeMap(Comparator.reverseOrder())) { it.variant.valueParameters.size }.let { m ->
            val firstKey = m.keys.first().cast<Int>()
            m.filter { it.key == firstKey }.map { it.value.cast() }
        }
    }
}