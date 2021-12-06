/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("PRE_RELEASE_CLASS", "ClassName", "RedundantVisibilityModifier", "KDocUnresolvedReference")

package net.mamoe.mirai.console.codegen

import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

internal object ValuePluginDataCodegen {
    /**
     * The interface
     */
    object PrimitiveValuesCodegen : RegionCodegen("Value.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            @Suppress("ClassName")
            appendKCode(
                """
                    /**
                     * 表示一个不可空 [$ktType] [Value].
                     */
                    public interface ${ktType}Value : PrimitiveValue<$ktType>
                """
            )
        }
    }

    object BuiltInSerializerConstantsPrimitivesCodegen : RegionCodegen("_PluginData.value.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            appendLine(
                kCode(
                    """
                @JvmStatic
                internal val ${ktType.standardName}SerializerDescriptor = ${ktType.standardName}.serializer().descriptor 
            """
                ).lines().joinToString("\n") { "    $it" }
            )
        }
    }

    object PrimitiveValuesImplCodegen : RegionCodegen("_PrimitiveValueDeclarations.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            appendKCode(
                """
internal abstract class ${ktType.standardName}ValueImpl : ${ktType.standardName}Value, SerializerAwareValue<${ktType.standardName}>, KSerializer<Unit>, AbstractValueImpl<${ktType.standardName}> {
    constructor()
    constructor(default: ${ktType.standardName}) {
        _value = default
    }

    private var _value: ${ktType.standardName}? = null

    final override var value: ${ktType.standardName}
        get() = _value ?: error("${ktType.standardName}Value.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.${ktType.standardName}SerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = ${ktType.standardName}.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(${ktType.standardName}.serializer().deserialize(decoder))
    override fun toString(): String = _value${if (ktType != KtString) "?.toString()" else ""} ?: "${ktType.standardName}Value.value not yet initialized."
    override fun equals(other: Any?): Boolean = other is ${ktType.standardName}ValueImpl && other::class.java == this::class.java && other._value == this._value
    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}
                """
            )
        }

    }

    object PluginData_value_PrimitivesImplCodegen : RegionCodegen("_PluginData.value.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            appendKCode(
                """
internal fun PluginData.valueImpl(default: ${ktType.standardName}): SerializerAwareValue<${ktType.standardName}> {
    return object : ${ktType.standardName}ValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun PluginData.${ktType.lowerCaseName}ValueImpl(): SerializerAwareValue<${ktType.standardName}> {
    return object : ${ktType.standardName}ValueImpl() {
        override fun onChanged() = this@${ktType.lowerCaseName}ValueImpl.onValueChanged(this)
    }
}
                """
            )
        }
    }

    object PluginData_valueImplPrimitiveCodegen : RegionCodegen("_PluginData.value.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            appendKCode(
                """
                ${ktType.standardName}::class -> ${ktType.lowerCaseName}ValueImpl()
                """.trimIndent()
            )
        }
    }

    object PluginData_value_primitivesCodegen : RegionCodegen("PluginData.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            @Suppress("unused")
            appendKCode(
                """
                /**
                 * 创建一个 [${ktType.standardName}] 类型的 [Value], 并设置初始值为 [default]
                 */
                public fun PluginData.value(default: ${ktType.standardName}): SerializerAwareValue<${ktType.standardName}> = valueImpl(default)
                """
            )
            appendLine()
        }

    }

    object JPluginData_value_primitivesCodegen : RegionCodegen("JPluginData.kt"), DefaultInvoke {
        @JvmStatic
        fun main(args: Array<String>) = super.startIndependently()
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            @Suppress("unused")
            appendKCode(
                """
                /**
                 * 创建一个 [${ktType.standardName}] 类型的 [Value], 并设置初始值为 [default]
                 */
                public fun value(default: ${ktType.standardName}): SerializerAwareValue<${ktType.standardName}> = delegate.valueImpl(default)
                """
            )
            appendLine()
        }

    }

    /**
     * 运行本 object 中所有嵌套 object Codegen
     */
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        ValuePluginDataCodegen::class.nestedClasses
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
}