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
internal class FileMessageImpl(
    override val name: String,
    override val id: String,
    override val size: Long,
    val busId: Int // internal // TODO: 2021/3/8 introduce OnlineFileMessage and OfflineFileMessage to eliminate property `busId`.
) : FileMessage {
    override fun toString(): String = "[mirai:file:$name,$id]"

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileMessageImpl

        if (name != other.name) return false
        if (id != other.id) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + busId.hashCode()
        return result
    }

}