/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.context

import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.components.PacketCodec
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.utils.crypto.ECDH

/**
 * Contains secrets for encryption and decryption during a session created by [SsoProcessor] and [PacketCodec].
 *
 * @see AccountSecrets
 */
internal interface SsoSession {
    var outgoingPacketSessionId: ByteArray

    /**
     * always 0 for now.
     */
    var loginState: Int
    val ecdh: ECDH

    // also present in AccountSecrets
    var wLoginSigInfo: WLoginSigInfo
    val randomKey: ByteArray
}