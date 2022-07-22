/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider

internal actual fun Ecdh.Companion.create(): Ecdh<*, *> =
    kotlin.runCatching {
        // try platform default EC/ECDH implementations first, which may have better performance
        // note that they may not work properly but being created successfully
        JceEcdh().apply {
            val keyPair = generateKeyPair()
            calculateShareKey(keyPair.public, keyPair.private)
            val encoded = exportPublicKey(keyPair.public)
            importPublicKey(encoded)
        }
    }.getOrElse {
        // fallback to BouncyCastle
        JceEcdhWithProvider(BouncyCastleProvider())
    }