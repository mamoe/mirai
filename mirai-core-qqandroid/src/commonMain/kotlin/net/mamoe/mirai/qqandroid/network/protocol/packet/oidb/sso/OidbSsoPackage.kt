/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.oidb.sso

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.protocol.packet.MessageMicro

/**
 * oidb_sso$OIDBSSOPkg
 */
@Serializable
class OidbSsoPackage(
    @SerialId(1) val command: Int, // uint
    @SerialId(2) val serviceType: Int, // uint
    @SerialId(3) val result: Int, // uint
    @SerialId(4) val bodyBuffer: ByteArray,
    @SerialId(5) val errorMessage: String,
    @SerialId(6) val clientVersion: String
) : MessageMicro




