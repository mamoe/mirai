/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import kotlin.math.absoluteValue
import kotlin.random.Random

@Serializable
class SyncCookie(
    @SerialId(1) val time1: Long? = null, // 1580277992
    @SerialId(2) val time: Long, // 1580277992
    @SerialId(3) val unknown1: Long = Random.nextLong().absoluteValue,// 678328038
    @SerialId(4) val unknown2: Long = Random.nextLong().absoluteValue, // 1687142153
    @SerialId(5) val const1: Long = const1_, // 1458467940
    @SerialId(11) val const2: Long = const2_, // 2683038258
    @SerialId(12) val unknown3: Long = 0x1d,
    @SerialId(13) val lastSyncTime: Long? = null,
    @SerialId(14) val unknown4: Long = 0
) : ProtoBuf

private val const1_: Long = Random.nextLong().absoluteValue
private val const2_: Long = Random.nextLong().absoluteValue
/*

@Serializable
class SyncCookie(
    @SerialId(1) val time1: Long? = null, // 1580277992
    @SerialId(2) val time: Long, // 1580277992
    @SerialId(3) val unknown1: Long = 678328038,// 678328038
    @SerialId(4) val unknown2: Long = 1687142153, // 1687142153
    @SerialId(5) val const1: Long = 1458467940, // 1458467940
    @SerialId(11) val const2: Long = 2683038258, // 2683038258
    @SerialId(12) val unknown3: Long = 0x1d,
    @SerialId(13) val lastSyncTime: Long? = null,
    @SerialId(14) val unknown4: Long = 0
) : ProtoBuf
 */