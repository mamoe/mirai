package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf

interface MessageMicro


fun <T : MessageMicro> T.toByteArray(serializer: SerializationStrategy<T>): ByteArray = ProtoBuf.dump(serializer, this)