/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import kotlin.jvm.JvmStatic

internal data class EcdhKeyPair<TPublicKey, TPrivate>(val public: TPublicKey, val private: TPrivate)

internal interface Ecdh<TPublicKey, TPrivate> {
    fun generateKeyPair(): EcdhKeyPair<TPublicKey, TPrivate>
    fun calculateShareKey(peerKey: TPublicKey, privateKey: TPrivate): ByteArray

    /**
     * @param encoded The encoding should conform with
     * Sec. 2.3.3 of the SECG SEC 1 ("Elliptic Curve Cryptography") standard,
     * with compression is off.
     * @see <a href="https://www.secg.org/sec1-v2.pdf">SECG SEC 1: Elliptic Curve Cryptography</a>
     */
    fun importPublicKey(encoded: ByteArray): TPublicKey

    /**
     * @return The encoding conforms with
     * Sec. 2.3.3 of the SECG SEC 1 ("Elliptic Curve Cryptography") standard,
     * with compression is off.
     * @see <a href="https://www.secg.org/sec1-v2.pdf">SECG SEC 1: Elliptic Curve Cryptography</a>
     */
    fun exportPublicKey(key: TPublicKey): ByteArray

    companion object {
        @JvmStatic
        val Instance by lazy {
            @Suppress("UNCHECKED_CAST")
            Ecdh.create() as Ecdh<Any, Any>
        }
    }
}
internal expect fun Ecdh.Companion.create() : Ecdh<*, *>