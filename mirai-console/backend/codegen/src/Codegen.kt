/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("FunctionName", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "PRE_RELEASE_CLASS", "unused")

package net.mamoe.mirai.console.codegen

import org.intellij.lang.annotations.Language
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

inline fun <reified T> runCodegenInObject() = runCodegenInObject(T::class)

fun runCodegenInObject(clazz: KClass<*>) {
    clazz.nestedClasses
        .filter { it.isSubclassOf(RegionCodegen::class) }
        .associateWith { kClass -> kClass.functions.find { it.name == "main" && it.hasAnnotation<JvmStatic>() } }
        .filter { it.value != null }
        .forEach { (kClass, entryPoint) ->
            println("---------------------------------------------")
            println("Running Codegen: ${kClass.simpleName}")
            entryPoint!!.call(kClass.objectInstance, arrayOf<String>())
            println("---------------------------------------------")
        }
}

abstract class Replacer(private val name: String) : (String) -> String {
    override fun toString(): String {
        return name
    }
}

fun Codegen.Replacer(block: (String) -> String): Replacer {
    return object : Replacer(this@Replacer::class.simpleName ?: "<unnamed>") {
        override fun invoke(p1: String): String = block(p1)
    }
}

class CodegenScope : MutableList<Replacer> by mutableListOf() {
    fun applyTo(fileContent: String): String {
        return this.fold(fileContent) { acc, replacer -> replacer(acc) }
    }

    @CodegenDsl
    operator fun Codegen.invoke(vararg ktTypes: KtType) {
        if (ktTypes.isEmpty() && this is DefaultInvoke) {
            invoke(defaultInvokeArgs)
        }
        invoke(ktTypes.toList())
    }

    @CodegenDsl
    operator fun Codegen.invoke(ktTypes: Collection<KtType>) {
        add(Replacer { str ->
            str + buildString {
                ktTypes.forEach { ktType -> applyTo(this, ktType) }
            }
        })
    }

    @RegionCodegenDsl
    operator fun RegionCodegen.invoke(vararg ktTypes: KtType) = invoke(ktTypes.toList())

    @RegionCodegenDsl
    operator fun RegionCodegen.invoke(ktTypes: Collection<KtType>) {
        add(Replacer { content ->
            content.replace(Regex("""//// region $regionName CODEGEN ////([\s\S]*?)( *)//// endregion $regionName CODEGEN ////""")) { result ->
                val indent = result.groups[2]!!.value
                val indentedCode = CodegenScope()
                    .apply { (this@invoke as Codegen).invoke(*ktTypes.toTypedArray()) } // add codegen task
                    .applyTo("") // perform codegen
                    .lines().dropLastWhile(String::isBlank).joinToString("\n") // remove blank following lines
                    .mapLine { "${indent}$it" } // indent
                """
                        |//// region $regionName CODEGEN ////
                        |
                        |${indentedCode}
                        |
                        |${indent}//// endregion $regionName CODEGEN ////
                    """.trimMargin()
            }
        })
    }

    @DslMarker
    annotation class CodegenDsl
}

internal fun String.mapLine(mapper: (String) -> CharSequence) = this.lines().joinToString("\n", transform = mapper)

@DslMarker
annotation class RegionCodegenDsl

interface DefaultInvoke {
    val defaultInvokeArgs: List<KtType>
}

abstract class Codegen {
    fun applyTo(stringBuilder: StringBuilder, ktType: KtType) = this.run { stringBuilder.apply(ktType) }

    protected abstract fun StringBuilder.apply(ktType: KtType)
}

abstract class RegionCodegen(private val targetFile: String, regionName: String? = null) : Codegen() {
    val regionName: String by lazy {
        regionName ?: this::class.simpleName!!.substringBefore("Codegen")
    }

    fun startIndependently() {
        codegen(targetFile) {
            this@RegionCodegen.invoke()
        }
    }
}

abstract class PrimitiveCodegen : Codegen() {
    protected abstract fun StringBuilder.apply(ktType: KtPrimitive)

    fun StringBuilder.apply(ktType: List<KtPrimitive>) = ktType.forEach { apply(it) }
}

fun StringBuilder.appendKCode(@Language("kt") ktCode: String): StringBuilder = append(kCode(ktCode)).appendLine()
