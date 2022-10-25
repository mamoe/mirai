package net.mamoe.mirai.clikt.testing

import io.kotest.matchers.shouldBe
import net.mamoe.mirai.clikt.core.Context
import net.mamoe.mirai.clikt.parameters.options.Option
import net.mamoe.mirai.clikt.sources.MapValueSource
import net.mamoe.mirai.clikt.sources.ValueSource

class TestSource(vararg values: Pair<String, String>) : ValueSource {
    private var read: Boolean = false
    private val source = MapValueSource(values.toMap())

    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        read = true
        return source.getValues(context, option)
    }

    fun assert(read: Boolean) {
        this.read shouldBe read
    }
}