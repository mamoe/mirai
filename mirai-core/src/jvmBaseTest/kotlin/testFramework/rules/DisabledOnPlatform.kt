/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.rules

import net.mamoe.mirai.internal.testFramework.Platform
import net.mamoe.mirai.internal.testFramework.currentPlatform
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf


@Suppress("NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS") // magic
actual typealias DisabledOnJvmLikePlatform = DisabledOnPlatform

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(DisabledOnPlatformCondition::class)
annotation class DisabledOnPlatform(
    vararg val values: KClass<out Platform>
)

internal object DisabledOnPlatformCondition : ExecutionCondition {
    private val ENABLED_BY_DEFAULT = ConditionEvaluationResult.enabled("@DisabledOnPlatform is not present")

    override fun evaluateExecutionCondition(context: ExtensionContext?): ConditionEvaluationResult {
        val annotation = AnnotationUtils.findAnnotation(
            context!!.element,
            DisabledOnPlatform::class.java
        ).getOrNull() ?: return ENABLED_BY_DEFAULT

        for (value in annotation.values) {
            check(value != Platform::class) { "@DisabledOnPlatform(Platform::class) is invalid. Use `@Disabled` to disable on all platforms" }
        }

        val currentPlatform = currentPlatform()
        val currentPlatformClass = currentPlatform::class
        annotation.values.find { it.isSuperclassOf(currentPlatformClass) }
            ?.let {
                return ConditionEvaluationResult.disabled("@DisabledOnPlatform(${it.simpleName}), current = $currentPlatform")
            }
        return ENABLED_BY_DEFAULT
    }
}