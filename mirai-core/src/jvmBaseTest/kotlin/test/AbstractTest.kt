/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.test.StandardTestDispatcher
import net.mamoe.mirai.internal.testFramework.TestFactory
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

@Timeout(value = 7, unit = TimeUnit.MINUTES)
actual abstract class AbstractTest actual constructor() {
    actual fun borrowSingleThreadDispatcher(): CoroutineDispatcher {
        return StandardTestDispatcher()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    actual companion object {
        @BeforeAll
        @JvmStatic
        fun checkTestFactories(testInfo: TestInfo) {
            val clazz = testInfo.testClass.getOrNull()?.kotlin ?: return
            for (function in clazz.functions) {
                if (function.hasAnnotation<TestFactory>()) {
                    check(function.returnType.classifier == List::class) {
                        "Illegal TestFactory function. A such function must return DynamicTestsResult."
                    }
                    check((function.returnType.classifier as? KClass<*>)?.qualifiedName == List::class.qualifiedName) {
                        "Illegal TestFactory function. A such function must return DynamicTestsResult."
                    }
                }
            }
        }

        init {
            initializeTestPlatformBeforeCommon()
            initializeTestCommon()

            runCatching { DebugProbes.install() }
        }
    }
}

internal expect fun initializeTestPlatformBeforeCommon() /* override MiraiLogger.Factory implementation on AndroidUnitTest */