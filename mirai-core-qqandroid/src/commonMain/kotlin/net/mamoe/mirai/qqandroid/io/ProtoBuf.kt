package net.mamoe.mirai.qqandroid.io

import kotlinx.io.core.Input
import kotlinx.io.core.readBytes
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/**
 * 仅有标示作用
 */
interface ProtoBuf

/**
 * dump
 */
fun <T : ProtoBuf> T.toByteArray(serializer: SerializationStrategy<T>): ByteArray {
    return kotlinx.serialization.protobuf.ProtoBuf.dump(serializer, this)
}

/**
 * load
 */
fun <T : ProtoBuf> ByteArray.loadAs(serializer: DeserializationStrategy<T>): T {
    return kotlinx.serialization.protobuf.ProtoBuf.load(serializer, this)
}

/**
 * load
 */
fun <T : ProtoBuf> Input.readRemainingAsProtoBuf(serializer: DeserializationStrategy<T>): T {
    return kotlinx.serialization.protobuf.ProtoBuf.load(serializer, this.readBytes())
}