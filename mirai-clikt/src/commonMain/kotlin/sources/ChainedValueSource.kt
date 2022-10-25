package net.mamoe.mirai.clikt.sources

import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.options.Option

/**
 * A [ValueSource] that looks for values in multiple other sources.
 */
public class ChainedValueSource(public val sources: List<ValueSource>) : ValueSource {
    init {
        require(sources.isNotEmpty()) { "Must provide configuration sources" }
    }

    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        return sources.asSequence()
            .map { it.getValues(context, option) }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
    }
}