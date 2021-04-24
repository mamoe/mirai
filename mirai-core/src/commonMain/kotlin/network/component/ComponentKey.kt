/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.component

import kotlin.reflect.*
import kotlin.reflect.full.allSupertypes

/**
 * A key for specific component [T]. Components are not polymorphic.
 *
 * @param T is a type hint.
 */
internal interface ComponentKey<T : Any> {
    /**
     * Get name of `T`.
     *
     * - If [qualified] is `false`, example: `PacketCodec`.
     * - If [qualified] is `true`, example: `net.mamoe.mirai.internal.network.components.PacketCodec`.
     */
    fun componentName(qualified: Boolean = false): String {
        val argument = getComponentTypeArgument()
        argument?.render(qualified)?.let { return it }
        return argument?.type?.classifier.renderClassifier(qualified)
    }

    fun smartToString(qualified: Boolean = false): String {
        return "ComponentKey<${componentName(qualified)}>"
    }

    companion object {
        // reflection is slow, but it is initialized once only (if memory sufficient).

        private fun KTypeProjection.render(
            fullName: Boolean
        ): String? {
            val projection = this

            projection.type?.classifier?.let { classifier ->
                if (classifier is KClass<*>) {
                    return classifier.run { if (fullName) qualifiedName else simpleName } ?: "?"
                }
            }

            projection.type?.arguments?.firstOrNull()?.let { argument ->
                return argument.render(fullName)
            }

            return null
        }

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
                1 -> upperBounds[0].renderType(fullName)
                else -> upperBounds.joinToString(" & ") { it.renderType(fullName) }
            }
        }

        private fun ComponentKey<*>.getComponentTypeArgument(): KTypeProjection? {
            val thisType = this::class.allSupertypes.find { it.classifier == COMPONENT_KEY_K_CLASS }
            return thisType?.arguments?.firstOrNull()
        }

        private val COMPONENT_KEY_K_CLASS = ComponentKey::class
    }
}