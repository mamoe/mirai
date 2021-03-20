/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.FileMessage
import kotlin.contracts.contract

internal fun FileMessage.checkIsImpl(): FileMessageImpl {
    contract { returns() implies (this@checkIsImpl is FileMessageImpl) }
    return this as? FileMessageImpl ?: error("FileMessage must not be implemented manually.")
}

@Serializable
@SerialName(FileMessage.SERIAL_NAME)
internal data class FileMessageImpl(
    override val id: String,
    @SerialName("internalId") val busId: Int,
    override val name: String,
    override val size: Long,
) : FileMessage {
    override val internalId: Int
        get() = busId

    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:file:")
        builder.append(id).append(",")
        builder.append(busId).append(",")
        builder.append(name).append(",")
        builder.append(size).append("]")
    }

    override fun toString(): String = "[mirai:file:$name,$id,$size,$busId]"
}