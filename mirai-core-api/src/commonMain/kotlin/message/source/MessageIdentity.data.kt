/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.source

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.MessageSourceKind

@Serializable
@SerialName("MessageIdentity")
public class RawMessageIdentity(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
) : MessageIdentity {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawMessageIdentity) return false

        if (!ids.contentEquals(other.ids)) return false
        if (!internalIds.contentEquals(other.internalIds)) return false
        return time == other.time
    }

    override fun hashCode(): Int {
        var result = ids.contentHashCode()
        result = 31 * result + internalIds.contentHashCode()
        result = 31 * result + time
        return result
    }

    override fun convertToRawMessageIdentity(): RawMessageIdentity {
        return this
    }

    override fun toString(): String {
        return "[mirai:message-identity:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, time=$time]"
    }
}

@Serializable
@SerialName("FullyMessageIdentity")
public class RawFullyMessageIdentity(
    override val ids: IntArray,
    override val internalIds: IntArray,
    override val time: Int,
    override val fromId: Long,
    override val targetId: Long,
    override val kind: MessageSourceKind,
) : FullyMessageIdentity {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawFullyMessageIdentity) return false

        if (!ids.contentEquals(other.ids)) return false
        if (!internalIds.contentEquals(other.internalIds)) return false
        if (time != other.time) return false
        if (fromId != other.fromId) return false
        if (targetId != other.targetId) return false
        return kind == other.kind
    }

    override fun hashCode(): Int {
        var result = ids.contentHashCode()
        result = 31 * result + internalIds.contentHashCode()
        result = 31 * result + time
        result = 31 * result + fromId.hashCode()
        result = 31 * result + targetId.hashCode()
        result = 31 * result + kind.hashCode()
        return result
    }

    override fun convertToRawFullyMessageIdentity(): RawFullyMessageIdentity {
        return this
    }

    override fun toString(): String {
        return "[mirai:message-identity:$kind, ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, time=$time, from $fromId to $targetId]"
    }
}


