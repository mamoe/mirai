/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.component

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.allSupertypes

/**
 * A key for specific component [T]. Component are not polymorphic.
 *
 * @param T is a type hint.
 */
internal interface ComponentKey<T : Any> {
    /**
     * Get name of `T`.
     *
     * - If [qualified] is `false`, example: `PacketCodec`.
     * - If [qualified] is `true`, example: `net.mamoe.mirai.internal.network.handler.components.PacketCodec`.
     */
    fun componentName(qualified: Boolean = false): String {
        return getComponentTypeArgumentClassifier().renderClassifier(fullName = qualified)
    }

    fun smartToString(qualified: Boolean = false): String {
        return "ComponentKey<${componentName(qualified)}>"
    }

    private companion object {
        private fun KClassifier?.renderClassifier(
            fullName: Boolean
        ): String {
            return when (val classifier = this) {
                null -> "?"
                is KClass<*> -> classifier.run { if (fullName) qualifiedName else simpleName } ?: "?"
                is KTypeParameter -> classifier.renderTypeParameter(fullName)
                else -> "?"
            }
        }

        private fun KType.renderType(fullName: Boolean) = classifier.renderClassifier(fullName)

        private fun KTypeParameter.renderTypeParameter(fullName: Boolean): String {
            val upperBounds = upperBounds
            return when (upperBounds.size) {
                0 -> toString()
                1 -> "ComponentKey<${upperBounds[0].renderType(fullName)}>"
                else -> "ComponentKey<${upperBounds.joinToString(" & ") { it.renderType(fullName) }}>"
            }
        }

        private fun ComponentKey<*>.getComponentTypeArgumentClassifier(): KClassifier? {
            val thisType = this::class.allSupertypes.find { it.classifier == COMPONENT_KEY_K_CLASS }
            return thisType?.arguments?.firstOrNull()?.type?.classifier
        }

        val COMPONENT_KEY_K_CLASS = ComponentKey::class
    }
}