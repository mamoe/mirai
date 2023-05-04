/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.test

import net.mamoe.mirai.internal.utils.StructureToStringTransformerNew
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import net.mamoe.mirai.utils.Services
import org.junit.jupiter.api.Test
import kotlin.test.assertIsNot

internal actual fun initializeTestPlatformBeforeCommon() {
    Services.register(
        net.mamoe.mirai.utils.StructureToStringTransformer::class.qualifiedName!!,
        StructureToStringTransformerNew::class.qualifiedName!!,
        ::StructureToStringTransformerNew
    )
    Services.registerAsOverride(
        MiraiLogger.Factory::class.qualifiedName!!,
        "net.mamoe.mirai.utils.MiraiLogger.Factory"
    ) {
        JvmLoggerFactory()
    }

    // force override
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge.setInstance(JvmLoggerFactory())
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge.freeze()

    println("[testFramework] Initialized loggers using JvmLoggerFactory")


}

internal class AndroidUnitTestPlatformTest : AbstractTest() {
    @Test
    fun usesStdoutLogger() {
        // PlatformLogger uses android.util.Log and will fail
        assertIsNot<PlatformLogger>(MiraiLogger.Factory.create(this::class))
    }
}