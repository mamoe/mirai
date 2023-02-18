/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:UseSerializers(UuidAsStringSerializer::class)

package cihelper.buildIndex

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializable
data class NextIndexResp(
    val moduleId: UUID,
    val branchId: UUID,
    val previousIndexId: UUID?,
    val previousIndexValue: UInt?,
    val newIndex: Index
)

@Serializable
data class Index(
    val id: UUID,
    val branchId: UUID,
    val commitRef: String,
    val value: UInt,
    val date: LocalDateTime
) {
    init {
        require(commitRef.length == 40) { "Invalid commit ref: '$commitRef'" }
    }
}


object UuidAsStringSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(String.serializer().deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        String.serializer().serialize(encoder, value.toString())
    }
}