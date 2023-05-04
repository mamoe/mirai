/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import java.security.Provider
import java.security.Security

internal actual fun Ecdh.Companion.create(): Ecdh<*, *> =
// WARNING: If you change the SDK version checks here, 
//          search for usages of `mirai.android.target.api.level` and see if you need to change elsewhere! 
    //          Especially in mirai-core/build.gradle.kts (configuring bouncy-castle dependency)

    if (kotlin.runCatching {
            // When running tests on JVM desktop, `ClassNotFoundException` will be got
            android.os.Build.VERSION.SDK_INT >= 23
        }.getOrDefault(false)) {
        // For newer Android, BC is deprecated, but AndroidKeyStore (default) handles ECDH well
        // Do not specify a provider as Google recommends
        JceEcdh()
    } else {
        // For older Android, AndroidKeyStore (default) is buggy and cannot handle EC key generation without tricks
        // See https://developer.android.com/training/articles/keystore#SupportedKeyPairGenerators for details

        // Let's use BC instead, BC is bundled into older Android
        JceEcdhWithProvider(
            Security.getProvider("BC")
                ?: Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider")
                    .getConstructor().newInstance() as Provider // in tests
        )
    }