/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.serializer
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.ProcessResult
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.*
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import kotlin.reflect.full.createType

/**
 * How to inject recorder?
 *
 * ```
 * bot.components[NoticeProcessorPipeline].registerProcessor(recorder)
 * ```
 */
internal class RecordingNoticeProcessor : SimpleNoticeProcessor<ProtocolStruct>(type()) {
    private val id = atomic(0)
    private val lock = Mutex()

    override suspend fun PipelineContext.processImpl(data: ProtocolStruct) {
        lock.withLock {
            id.getAndDecrement()
            logger.info { "Recorded #${id.value} ${data::class.simpleName}" }
            val serial = serialize(this, data)
            logger.info { "original:     $serial" }
            logger.info { "desensitized: " + desensitize(serial) }
            logger.info { "decoded: " + deserialize(desensitize(serial)).struct._miraiContentToString() }
        }
    }

    @Serializable
    data class RecordNode(
        val structType: String,
        val struct: String,
        val attributes: Map<String, String>,
    )

    @Serializable
    data class DeserializedRecord(
        val attributes: TypeSafeMap,
        val struct: ProtocolStruct
    )

    companion object {
        private val logger = MiraiLogger.Factory.create(RecordingNoticeProcessor::class)

        private val yaml = Yaml {
            // one-line
            classSerialization = YamlBuilder.MapSerialization.FLOW_MAP
            mapSerialization = YamlBuilder.MapSerialization.FLOW_MAP
            listSerialization = YamlBuilder.ListSerialization.FLOW_SEQUENCE
            stringSerialization = YamlBuilder.StringSerialization.DOUBLE_QUOTATION
            encodeDefaultValues = false
        }

        fun serialize(context: PipelineContext, data: ProtocolStruct): String {
            return serialize(context.attributes.toMap(), data)
        }

        fun serialize(attributes: Map<String, @Contextual Any?>, data: ProtocolStruct): String {
            return yaml.encodeToString(
                RecordNode(
                    data::class.java.name,
                    yaml.encodeToString(data),
                    attributes.mapValues { yaml.encodeToString(it.value) })
            )
        }

        fun deserialize(string: String): DeserializedRecord {
            val (type, struct, attributes) = yaml.decodeFromString(RecordNode.serializer(), string)
            val serializer = serializer(Class.forName(type).kotlin.createType())
            return DeserializedRecord(
                TypeSafeMap(attributes.mapValues { yaml.decodeAnyFromString(it.value) }),
                yaml.decodeFromString(serializer, struct).cast()
            )
        }

        private val desensitizer by lateinitMutableProperty {
            Desensitizer.create(
                run<Map<String, String>> {

                    val filename =
                        systemProp("mirai.network.recording.desensitization.filepath", "local.desensitization.yml")

                    val file =
                        Thread.currentThread().contextClassLoader.getResource(filename)
                            ?: Thread.currentThread().contextClassLoader.getResource("recording/configs/$filename")
                            ?: error("Could not find desensitization configuration!")

                    yaml.decodeFromString(file.readText())
                }.also {
                    logger.info { "Loaded ${it.size} desensitization rules." }
                }
            )
        }

        fun desensitize(string: String): String = desensitizer.desensitize(string)
    }
}

internal suspend fun NoticeProcessorPipeline.processRecording(
    bot: QQAndroidBot,
    record: RecordingNoticeProcessor.DeserializedRecord
): ProcessResult {
    return this.process(bot, record.struct, record.attributes)
}

internal class Desensitizer private constructor(
    val rules: Map<String, String>,
) {
    companion object {
        fun create(rules: Map<String, String>): Desensitizer {
            val map = HashMap<String, String>()
            map.putAll(rules)
            rules.forEach { (t, u) ->
                if (t.toLongOrNull() != null && u.toLongOrNull() != null) {
                    map.putIfAbsent(
                        Mirai.calculateGroupUinByGroupCode(t.toLong()).toString(),
                        Mirai.calculateGroupUinByGroupCode(u.toLong()).toString()
                    )
                }
            }
            return Desensitizer(rules)
        }
    }

    fun desensitize(value: String): String {
        return rules.entries.fold(value) { acc, entry ->
            acc.replace(entry.key, entry.value)
        }
    }
}