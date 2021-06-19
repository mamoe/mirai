/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.test

import net.mamoe.mirai.utils.MiraiLogger
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Test
import java.security.Security
import kotlin.test.assertTrue

internal actual fun initPlatform() {
    init
}

private val init: Unit by lazy {
    MiraiLogger.setDefaultLoggerCreator {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        net.mamoe.mirai.internal.utils.StdoutLogger(it)
    }

    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
    }
    Security.addProvider(BouncyCastleProvider())

    Unit
}

internal actual class PlatformInitializationTest : AbstractTest() {

    @Test
    actual fun test() {
        assertTrue {
            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
            MiraiLogger.create("1") is net.mamoe.mirai.internal.utils.StdoutLogger
        }
    }
}