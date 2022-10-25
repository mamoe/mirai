package net.mamoe.mirai.clikt.sources

import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.options.Option
import net.mamoe.mirai.clikt.parameters.options.pair
import net.mamoe.mirai.clikt.parameters.options.triple

/**
 * A [ValueSource] that reads values from a map.
 *
 * This implementation will only return a single value for each option. If you use conversions like
 * [pair] or [triple], you'll need to implement a [ValueSource] yourself.
 *
 * @param values The map of key to value for each option
 * @param getKey A function that return the key in [values] for a given option. By default, it joins the
 */
public class MapValueSource(
    private val values: Map<String, String>,
    private val getKey: (Context, Option) -> String = ValueSource.getKey(joinSubcommands = "."),
) : ValueSource {
    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        return values[option.valueSourceKey ?: getKey(context, option)]
            ?.let { ValueSource.Invocation.just(it) }.orEmpty()
    }
}