/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl

internal val JsonForCache = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    isLenient = true
}

@Serializable
internal data class FriendListCache(
    var friendListSeq: Long = 0,
    /**
     * 实际上是个序列号, 不是时间
     */
    var timeStamp: Long = 0,
    var list: List<FriendInfoImpl> = emptyList(),
)