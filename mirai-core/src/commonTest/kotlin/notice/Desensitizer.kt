/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice

import kotlinx.serialization.decodeFromString
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.notice.Desensitizer.Companion.generateAndDesensitize
import net.mamoe.mirai.internal.utils.codegen.*
import net.mamoe.mirai.internal.utils.io.NestedStructure
import net.mamoe.mirai.internal.utils.io.NestedStructureDesensitizer
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.*
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import java.io.File
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.typeOf

private val logger: MiraiLogger by lazy { MiraiLogger.Factory.create(Desensitizer::class) }

internal class Desensitizer private constructor(
    val rules: Map<String, String>,
) {
    fun desensitize(value: String): String {
        return rules.entries.fold(value) { acc, entry ->
            acc.replace(entry.key, entry.value)
        }
    }

    fun desensitize(value: ByteArray): ByteArray {
        return desensitize(value.toUHexString()).hexToBytes()
    }

    fun desensitize(value: Array<Byte>): Array<Byte> {
        return desensitize(value.toUHexString()).hexToBytes().toTypedArray()
    }


    companion object {
        private val instance by lateinitMutableProperty {
            create(
                run<Map<String, String>> {

                    val filename =
                        systemProp("mirai.network.recording.desensitization.filepath", "local.desensitization.yml")

                    val file =
                        File(filename).takeIf { it.isFile }?.toURI()?.toURL()
                            ?: Thread.currentThread().contextClassLoader.getResource(filename)
                            ?: Thread.currentThread().contextClassLoader.getResource("recording/configs/$filename")
                            ?: error("Could not find desensitization configuration!")

                    format.decodeFromString(file.readText())
                }.also {
                    logger.info { "Loaded ${it.size} desensitization rules." }
                }
            )
        }

        /**
         * Loaded from local.desensitization.yml
         */
        val local get() = instance

        fun desensitize(string: String): String = instance.desensitize(string)


        fun ConstructorCallCodegenFacade.generateAndDesensitize(
            value: Any?,
            type: KType,
            desensitizer: Desensitizer = instance,
        ): String {
            val a = analyze(value, type).apply {
                accept(DesensitizationVisitor(desensitizer))
            }
            return generate(a)
        }

        @OptIn(ExperimentalStdlibApi::class)
        inline fun <reified T> ConstructorCallCodegenFacade.generateAndDesensitize(
            value: T,
            desensitizer: Desensitizer = instance,
        ): String = generateAndDesensitize(value, typeOf<T>(), desensitizer)


        fun create(rules: Map<String, String>): Desensitizer {
            val map = HashMap<String, String>()
            map.putAll(rules)

            fun addExtraRulesForString(value: String, replacement: String) {
                // in proto, strings have lengths field, we must ensure that their lengths are intact.

                when {
                    value.length > replacement.length -> {
                        map[value.toByteArray().toUHexString()] =
                            (replacement + "0".repeat(value.length - replacement.length)).toByteArray()
                                .toUHexString() // fix it to the same length
                    }
                    value.length < replacement.length -> {
                        error("Replacement '$replacement' must not be longer than '$value'")
                    }
                    else -> {
                        map.putIfAbsent(value.toByteArray().toUHexString(), replacement.toByteArray().toUHexString())
                    }
                }
            }

            fun addExtraRulesForNumber(value: Long, replacement: Long) {
                map.putIfAbsent(value.toString(), replacement.toString())

                // 某些地方会 readLong, readInt, desensitizer visit 不到这些目标
                map.putIfAbsent(value.toByteArray().toUHexString(), replacement.toByteArray().toUHexString())

                if (value in Int.MIN_VALUE.toLong()..UInt.MAX_VALUE.toLong()
                    && replacement in Int.MIN_VALUE.toLong()..UInt.MAX_VALUE.toLong()
                ) {
                    map.putIfAbsent(
                        value.toInt().toByteArray().toUHexString(),
                        replacement.toInt().toByteArray().toUHexString()
                    )
                }
                // 不需要处理 proto, 所有 proto 都会被反序列化为结构类型由 desensitizer 处理
            }

            rules.forEach { (t, u) ->
                if (t.toLongOrNull() != null && u.toLongOrNull() != null) {
                    addExtraRulesForNumber(t.toLong(), u.toLong())
                    @Suppress("DEPRECATION")
                    addExtraRulesForNumber(
                        Mirai.calculateGroupUinByGroupCode(t.toLong()),
                        Mirai.calculateGroupUinByGroupCode(u.toLong())
                    ) // putIfAbsent, code prevails
                }

                addExtraRulesForString(t, u)
            }

            return Desensitizer(map)
        }
    }
}

private val format = Yaml {
    // one-line
    classSerialization = YamlBuilder.MapSerialization.FLOW_MAP
    mapSerialization = YamlBuilder.MapSerialization.FLOW_MAP
    listSerialization = YamlBuilder.ListSerialization.FLOW_SEQUENCE
    stringSerialization = YamlBuilder.StringSerialization.DOUBLE_QUOTATION
    encodeDefaultValues = false
}


private class DesensitizationVisitor(
    private val desensitizer: Desensitizer,
) : ValueDescVisitor {
    override fun visitPlain(desc: PlainValueDesc) {
        desc.value = desensitizer.desensitize(desc.value)
    }

    override fun visitObjectArray(desc: ObjectArrayValueDesc) {
        if (desc.arrayType.arguments.first().type?.classifier == Byte::class) { // variance is ignored
            @Suppress("UNCHECKED_CAST")
            desc.value = desensitizer.desensitize(desc.value as Array<Byte>)
        } else {
            for (element in desc.elements) {
                element.accept(this)
            }
        }
    }

    override fun visitCollection(desc: CollectionValueDesc) {
        for (element in desc.elements) {
            element.accept(this)
        }
    }

    override fun visitPrimitiveArray(desc: PrimitiveArrayValueDesc) {
        if (desc.value is ByteArray) {
            desc.value = desensitizer.desensitize(desc.value as ByteArray)
        }
    }

    override fun <T : Any> visitClass(desc: ClassValueDesc<T>) {
        super.visitClass(desc)
        desc.properties.replaceAll() { key, value ->
            val annotation = key.findAnnotation<NestedStructure>()
            if (annotation != null && value.origin is ByteArray) {
                val instance = annotation.serializer.objectInstance ?: annotation.serializer.createInstance()

                val result = instance.cast<NestedStructureDesensitizer<ProtocolStruct, ProtocolStruct>>()
                    .deserialize(desc.origin as ProtocolStruct, value.origin as ByteArray)
                    ?: desc.origin

                val generate = ConstructorCallCodegenFacade.generateAndDesensitize(result)
                PlainValueDesc(
                    desc,
                    "$generate.toByteArray(${result::class.qualifiedName}.serializer())",
                    value.origin
                )
            } else value
        }
    }
}