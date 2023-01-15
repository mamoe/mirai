/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.vote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.isSameClass
import kotlin.jvm.JvmStatic


/**
 * 群投票图片. 可通过 [Votes.uploadImage] 上传获得. 不确定服务器会保存多久.
 *
 * 要发布一条带有图片的投票, 请在构造 [VoteParameters] 时提供 [VoteParameters.image] 参数. 详见 [Vote].
 *
 * @since 2.14
 */
@SerialName(VoteImage.SERIAL_NAME)
@Serializable
public class VoteImage private constructor(
    public val id: String,
    public val height: Int,
    public val width: Int,
) {
    // For stability, do not make it `data class`.

    public companion object {
        public const val SERIAL_NAME: String = "VoteImage"

        /**
         * 创建 [VoteImage] 实例.
         */
        @JvmStatic
        public fun create(id: String, height: Int, width: Int): VoteImage {
            return VoteImage(id, height, width)
        }
    }

    override fun toString(): String {
        return "VoteImage(id='$id', height=$height, width=$width)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VoteImage || !isSameClass(this, other)) return false

        if (id != other.id) return false
        if (height != other.height) return false
        if (width != other.width) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + width.hashCode()
        return result
    }
}