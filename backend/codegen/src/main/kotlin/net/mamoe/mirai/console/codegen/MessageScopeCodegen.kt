/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.codegen

internal val TypeCandidatesForMessageScope = arrayOf(
    KtType("Contact"),
    KtType("CommandSender"),
)

internal val KtMessageScope = KtType("MessageScope")

internal fun <A> Array<A>.arrangements(): List<Pair<A, A>> {
    val result = mutableListOf<Pair<A, A>>()
    for (a in this) {
        for (b in this) {
            result.add(a to b)
        }
    }
    return result
}

internal fun <A> Array<A>.distinctArrangements(): List<Pair<A, A>> {
    return this.arrangements().distinctBy { it.first.toString().hashCode() + it.second.toString().hashCode() }
}

internal object MessageScopeCodegen {
    object IterableMessageScopeBuildersCodegen : RegionCodegen("MessageScope.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = listOf(KtString) // invoke once

        @Suppress(
            "RedundantVisibilityModifier", "ClassName", "KDocUnresolvedReference", "RedundantSuspendModifier",
            "SpellCheckingInspection"
        )
        override fun StringBuilder.apply(ktType: KtType) {
            for (collectionName in arrayOf("Iterable", "Sequence", "Array")) {
                for (candidate in (TypeCandidatesForMessageScope + KtMessageScope)) {
                    appendKCode(
                        """
                                @JvmName("toMessageScope${candidate.standardName.capitalize()}${collectionName.capitalize()}")
                                public fun $collectionName<$candidate?>.toMessageScope(): MessageScope {
                                    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope -> CombinedScope(acc, messageScope.asMessageScopeOrNoop()) }
                                }
                                """
                    )
                    appendLine()
                }
            }
            for (candidate in (TypeCandidatesForMessageScope + KtMessageScope)) {
                appendKCode(
                    """
                    @JvmSynthetic
                    @JvmName("toMessageScope${candidate.standardName.capitalize()}Flow")
                    public suspend fun Flow<$candidate>.toMessageScope(): MessageScope { // Flow<Any?>.firstOrNull isn't yet supported
                        return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope -> CombinedScope(acc, messageScope.asMessageScope()) }
                    }
                    """
                )
                appendLine()
            }
        }
    }

    object MessageScopeBuildersCodegen : RegionCodegen("MessageScope.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = listOf(KtString) // invoke once

        @Suppress("RedundantVisibilityModifier", "ClassName", "KDocUnresolvedReference", "unused")
        override fun StringBuilder.apply(ktType: KtType) {
            for (candidate in TypeCandidatesForMessageScope) {
                appendKCode(
                    """
                       public fun ${candidate}.asMessageScope(): MessageScope = createScopeDelegate(this)
                    """
                )
                appendLine()
            }
//
//            for ((a, b) in (TypeCandidatesForMessageScope + KtMessageScope).arrangements()) {
//                appendKCode(
//                    """
//                    @LowPriorityInOverloadResolution
//                    public fun ${a}.scopeWith(vararg others: ${b}): MessageScope {
//                        return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
//                    }
//                """
//                )
//                appendLine()
//            }

            for ((a, b) in (TypeCandidatesForMessageScope + KtMessageScope).arrangements()) {
                appendKCode(
                    """
                    @LowPriorityInOverloadResolution
                    public fun ${a}?.scopeWith(vararg others: ${b}?): MessageScope {
                        return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWith(other?.asMessageScope()) }
                    }
                """
                )
                appendLine()
            }
//
//            for ((a, b) in (TypeCandidatesForMessageScope + KtMessageScope).arrangements()) {
//                appendKCode(
//                    """
//                    public fun ${a}.scopeWith(other: ${b}): MessageScope {
//                        return CombinedScope(asMessageScope(), other.asMessageScope())
//                    }
//                """
//                )
//                appendLine()
//            }

            for ((a, b) in (TypeCandidatesForMessageScope + KtMessageScope).arrangements()) {
                appendKCode(
                    """
                    public fun ${a}?.scopeWith(other: ${b}?): MessageScope {
                        @Suppress("DuplicatedCode")
                        return when {
                            this == null && other == null -> NoopMessageScope
                            this == null && other != null -> other.asMessageScope()
                            this != null && other == null -> this.asMessageScope()
                            this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
                            else -> null!!
                        }
                    }
                """
                )
                appendLine()
            }
//
//            for ((a, b) in (TypeCandidatesForMessageScope + KtMessageScope).arrangements()) {
//                appendKCode(
//                    """
//                    public inline fun <R> ${a}.scopeWith(vararg others: ${b}, action: MessageScope.() -> R): R {
//                        return scopeWith(*others).invoke(action)
//                    }
//                """
//                )
//                appendLine()
//            }

            for ((a, b) in (TypeCandidatesForMessageScope + KtMessageScope).arrangements()) {
                appendKCode(
                    """
                    public inline fun <R> ${a}?.scopeWith(vararg others: ${b}?, action: MessageScope.() -> R): R {
                        return scopeWith(*others).invoke(action)
                    }
                """
                )
                appendLine()
            }

            for (a in (TypeCandidatesForMessageScope + KtMessageScope)) {
                appendKCode(
                    """
                    @Deprecated(
                        "Senseless scopeWith. Use asMessageScope.",
                        ReplaceWith("this.asMessageScope()", "net.mamoe.mirai.console.util.asMessageScope")
                    )
                    public inline fun ${a}.scopeWith(): MessageScope = asMessageScope()
                """
                )
                appendLine()
            }

            for (a in (TypeCandidatesForMessageScope + KtMessageScope)) {
                appendKCode(
                    """
                    @Deprecated(
                        "Senseless scopeWith. Use .asMessageScope().invoke.",
                        ReplaceWith(
                            "this.asMessageScope()(action)",
                            "net.mamoe.mirai.console.util.asMessageScope", 
                            "net.mamoe.mirai.console.util.invoke"
                        )
                    )
                    public inline fun <R> ${a}.scopeWith(action: MessageScope.() -> R): R = asMessageScope()(action)
"""
                )
                appendLine()
            }

        }
    }


    /**
     * 运行本 object 中所有嵌套 object Codegen
     */
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        runCodegenInObject(this::class)
    }
}