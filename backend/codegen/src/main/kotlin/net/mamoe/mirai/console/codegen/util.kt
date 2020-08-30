/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate", "unused", "PRE_RELEASE_CLASS")

package net.mamoe.mirai.console.codegen

import org.intellij.lang.annotations.Language
import java.io.File


typealias KtByte = KtType.KtPrimitive.KtByte
typealias KtShort = KtType.KtPrimitive.KtShort
typealias KtInt = KtType.KtPrimitive.KtInt
typealias KtLong = KtType.KtPrimitive.KtLong
typealias KtFloat = KtType.KtPrimitive.KtFloat
typealias KtDouble = KtType.KtPrimitive.KtDouble
typealias KtChar = KtType.KtPrimitive.KtChar
typealias KtBoolean = KtType.KtPrimitive.KtBoolean

typealias KtString = KtType.KtString

typealias KtCollection = KtType.KtCollection
typealias KtMap = KtType.KtMap

typealias KtPrimitive = KtType.KtPrimitive


sealed class KtType {
    /**
     * Its classname in standard library
     */
    abstract val standardName: String
    override fun toString(): String = standardName

    /**
     * Not Including [String]
     */
    sealed class KtPrimitive(
        override val standardName: String,
        val jPrimitiveName: String = standardName.toLowerCase(),
        val jObjectName: String = standardName
    ) : KtType() {
        object KtByte : KtPrimitive("Byte")
        object KtShort : KtPrimitive("Short")
        object KtInt : KtPrimitive("Int", jObjectName = "Integer")
        object KtLong : KtPrimitive("Long")

        object KtFloat : KtPrimitive("Float")
        object KtDouble : KtPrimitive("Double")

        object KtChar : KtPrimitive("Char", jObjectName = "Character")
        object KtBoolean : KtPrimitive("Boolean")
    }

    object KtString : KtType() {
        override val standardName: String get() = "String"
    }

    /**
     * [List], [Set]
     */
    data class KtCollection(override val standardName: String) : KtType()

    object KtMap : KtType() {
        override val standardName: String get() = "Map"
    }

    data class Custom(override val standardName: String) : KtType() {
        override fun toString(): String {
            return standardName
        }
    }

    companion object {
        operator fun invoke(standardName: String): KtType = Custom(standardName)
    }
}

val KtPrimitiveIntegers = listOf(KtByte, KtShort, KtInt, KtLong)
val KtPrimitiveFloatings = listOf(KtFloat, KtDouble)

val KtPrimitiveNumbers = KtPrimitiveIntegers + KtPrimitiveFloatings
val KtPrimitiveNonNumbers = listOf(KtChar, KtBoolean)

val KtPrimitives = KtPrimitiveNumbers + KtPrimitiveNonNumbers

operator fun KtType.plus(type: KtType): List<KtType> {
    return listOf(this, type)
}

val KtType.lowerCaseName: String get() = this.standardName.toLowerCase()

inline fun kCode(@Language("kt") source: String) = source.trimIndent()

fun codegen(targetFile: String, block: CodegenScope.() -> Unit) {
    //// region PrimitiveValue CODEGEN ////
    //// region PrimitiveValue CODEGEN ////

    targetFile.findFileSmart().also {
        println("Codegen target: ${it.absolutePath}")
    }.apply {
        writeText(
            CodegenScope().apply(block).also { list ->
                list.forEach {
                    println("Applying replacement: $it")
                }
            }.applyTo(readText())
        )
    }
}

fun String.findFileSmart(): File = kotlin.run {
    if (contains("/")) { // absolute
        File(this)
    } else {
        val list = File(".").walk().filter { it.name == this }.toList()
        if (list.isNotEmpty()) return list.single()

        File(".").walk().filter { it.name.contains(this) }.single()
    }
}.also {
    require(it.exists()) { "file doesn't exist" }
}