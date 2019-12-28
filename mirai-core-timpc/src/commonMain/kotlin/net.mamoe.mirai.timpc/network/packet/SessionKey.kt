package net.mamoe.mirai.timpc.network.packet

import net.mamoe.mirai.utils.cryptor.DecrypterByteArray
import net.mamoe.mirai.utils.cryptor.DecrypterType

/**
 * 会话密匙
 */
inline class SessionKey(override val value: ByteArray) : DecrypterByteArray {
    companion object Type : DecrypterType<SessionKey>
}
