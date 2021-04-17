/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.context

import kotlinx.io.core.toByteArray
import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.network.LoginExtraData
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.getRandomByteArray
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.get_mpasswd
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.toByteArray
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Secrets for authentication with server. (login)
 */
internal interface AccountSecrets {
    var wLoginSigInfoField: WLoginSigInfo?

    val wLoginSigInfoInitialized get() = wLoginSigInfoField != null
    var wLoginSigInfo: WLoginSigInfo
        get() = wLoginSigInfoField ?: error("wLoginSigInfoField is not yet initialized")
        set(value) {
            wLoginSigInfoField = value
        }

    /**
     * t537
     */
    var loginExtraData: MutableSet<LoginExtraData>

    var G: ByteArray // sigInfo[2]
    var dpwd: ByteArray
    var randSeed: ByteArray // t403

    /**
     * t108 时更新
     */
    var ksid: ByteArray

    var tgtgtKey: ByteArray
    val randomKey: ByteArray
}

@Suppress("ArrayInDataClass") // for `copy`
@Serializable
internal data class AccountSecretsImpl(
    override var loginExtraData: MutableSet<LoginExtraData>,
    override var wLoginSigInfoField: WLoginSigInfo?,
    override var G: ByteArray,
    override var dpwd: ByteArray = get_mpasswd().toByteArray(),
    override var randSeed: ByteArray,
    override var ksid: ByteArray,
    override var tgtgtKey: ByteArray,
    override val randomKey: ByteArray,
) : AccountSecrets, ProtoBuf

internal fun AccountSecretsImpl(
    other: AccountSecrets,
): AccountSecretsImpl = other.run {
    AccountSecretsImpl(loginExtraData, wLoginSigInfoField, G, dpwd, randSeed, ksid, tgtgtKey, randomKey)
}

internal fun AccountSecretsImpl(
    device: DeviceInfo, account: BotAccount
): AccountSecretsImpl {
    return AccountSecretsImpl(
        loginExtraData = CopyOnWriteArraySet(),
        wLoginSigInfoField = null,
        G = device.guid,
        dpwd = get_mpasswd().toByteArray(),
        randSeed = EMPTY_BYTE_ARRAY,
        ksid = EMPTY_BYTE_ARRAY,
        tgtgtKey = (account.passwordMd5 + ByteArray(4) + account.id.toInt().toByteArray()).md5(),
        randomKey = getRandomByteArray(16),
    )
}