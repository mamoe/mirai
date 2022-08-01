/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import kotlin.jvm.JvmField
import kotlin.math.absoluteValue
import kotlin.random.Random


// COMMENTED ON 2020/7/25

@Serializable
internal class SyncCookie(
    @ProtoNumber(1) @JvmField val time1: Long? = null, // 1580277992
    @ProtoNumber(2) @JvmField val time: Long, // 1580277992
    @ProtoNumber(3) @JvmField val unknown1: Long = Random.nextLong().absoluteValue, // 678328038
    @ProtoNumber(4) @JvmField val unknown2: Long = Random.nextLong().absoluteValue, // 1687142153
    @ProtoNumber(5) @JvmField val const1: Long = const1_, // 1458467940
    @ProtoNumber(11) @JvmField val const2: Long = const2_, // 2683038258
    @ProtoNumber(12) @JvmField val unknown3: Long = 0x1d,
    @ProtoNumber(13) @JvmField val lastSyncTime: Long? = null,
    @ProtoNumber(14) @JvmField val unknown4: Long = 0,
) : ProtoBuf

private val const1_: Long = Random.nextLong().absoluteValue
private val const2_: Long = Random.nextLong().absoluteValue


/*

@Serializable
internal class SyncCookie(
    @SerialId(1) @JvmField val time1: Long? = null, // 1580277992
    @SerialId(2) @JvmField val time: Long, // 1580277992
    @SerialId(3) @JvmField val unknown1: Long = 678328038,// 678328038
    @SerialId(4) @JvmField val unknown2: Long = 1687142153, // 1687142153
    @SerialId(5) @JvmField val const1: Long = 1458467940, // 1458467940
    @SerialId(11) @JvmField val const2: Long = 2683038258, // 2683038258
    @SerialId(12) @JvmField val unknown3: Long = 0x1d,
    @SerialId(13) @JvmField val lastSyncTime: Long? = null,
    @SerialId(14) @JvmField val unknown4: Long = 0
) : ProtoBuf
 */