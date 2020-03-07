package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.core.Input
import kotlinx.io.core.Output
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy

interface IOFormat : SerialFormat {

    fun <T> dump(serializer: SerializationStrategy<T>, output: Output): ByteArray

    fun <T> load(deserializer: DeserializationStrategy<T>, input: Input): T
}
