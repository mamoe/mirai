package net.mamoe.mirai.timpc.utils

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.writeFully
import kotlinx.serialization.SerializationStrategy
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBuf

fun <T> BytePacketBuilder.writeProto(serializer: SerializationStrategy<T>, obj: T) = writeFully(ProtoBuf.dump(serializer, obj))
