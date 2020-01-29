package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.serialization.SerializationStrategy
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBufWithNullableSupport

interface MessageMicro


fun <T : MessageMicro> T.toByteArray(serializer: SerializationStrategy<T>): ByteArray = ProtoBufWithNullableSupport.dump(serializer, this)