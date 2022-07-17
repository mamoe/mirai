/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework

actual typealias TestFactory = org.junit.jupiter.api.TestFactory

actual typealias DynamicTest = org.junit.jupiter.api.DynamicTest

@Suppress("ACTUAL_TYPE_ALIAS_TO_CLASS_WITH_DECLARATION_SITE_VARIANCE", "ACTUAL_WITHOUT_EXPECT")
actual typealias DynamicTestsResult = List<*>

actual fun dynamicTest(displayName: String, action: () -> Unit): DynamicTest {
    return DynamicTest.dynamicTest(displayName, action)
}


actual fun runDynamicTests(dynamicTests: List<DynamicTest>): DynamicTestsResult = dynamicTests