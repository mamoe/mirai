package net.mamoe.mirai.qqandroid.io

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.Input
import kotlinx.io.core.readBytes
import kotlinx.io.core.writeFully
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBufWithNullableSupport

/**
 * 仅有标示作用
 */
interface ProtoBuf

fun <T : ProtoBuf> BytePacketBuilder.writeProtoBuf(serializer: SerializationStrategy<T>, v: T) {
    this.writeFully(v.toByteArray(serializer))
}

/**
 * dump
 */
fun <T : ProtoBuf> T.toByteArray(serializer: SerializationStrategy<T>): ByteArray {
    return ProtoBufWithNullableSupport.dump(serializer, this)
}

/**
 * load
 */
fun <T : ProtoBuf> ByteArray.loadAs(deserializer: DeserializationStrategy<T>): T {
    return ProtoBufWithNullableSupport.load(deserializer, this)
}

/**
 * load
 */
fun <T : ProtoBuf> Input.readRemainingAsProtoBuf(serializer: DeserializationStrategy<T>): T {
    return ProtoBufWithNullableSupport.load(serializer, this.readBytes())
}