package net.mamoe.mirai.qqandroid.io.serialization

import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.Output
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy

interface IOFormat : SerialFormat {

    fun <T> dump(serializer: SerializationStrategy<T>, input: Input): ByteArray

    fun <T> load(deserializer: DeserializationStrategy<T>, output: Output): T
}
