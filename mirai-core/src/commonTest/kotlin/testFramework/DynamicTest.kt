/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("DynamicTestKt_common")

package net.mamoe.mirai.internal.testFramework

import kotlin.jvm.JvmName
import kotlin.test.Test

/**
 * Annotates a function to be a [Test] factory that returns a [DynamicTestsResult].
 *
 * On JVM, this delegates to JUnit's `TestFactory`. On Native, test functions are executed in-place.
 *
 * Tips: To run [TestFactory]s in IDEA, you can press `Ctrl + Shift + R`(for both macOS and Windows) with caret in the test function.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
expect annotation class TestFactory()

/**
 * @see dynamicTest
 */
expect open class DynamicTest // = junit.DynamicTest

/**
 * @see runDynamicTests
 */
expect class DynamicTestsResult

/**
 * Creates a dynamic test
 */
expect fun dynamicTest(displayName: String, action: () -> Unit): DynamicTest

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
expect fun runDynamicTests(dynamicTests: List<DynamicTest>): DynamicTestsResult

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(vararg dynamicTests: Iterable<DynamicTest>): DynamicTestsResult =
    runDynamicTests(dynamicTests = dynamicTests.flatMap { it })

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(vararg dynamicTests: Sequence<DynamicTest>): DynamicTestsResult =
    runDynamicTests(dynamicTests = dynamicTests.flatMap { it })

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(vararg dynamicTests: DynamicTest): DynamicTestsResult = runDynamicTests(dynamicTests.toList())

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(dynamicTests: Sequence<DynamicTest>): DynamicTestsResult = runDynamicTests(dynamicTests.toList())
