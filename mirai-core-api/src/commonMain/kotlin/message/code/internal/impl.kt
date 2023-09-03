/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER")

package net.mamoe.mirai.message.code.internal

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi


internal fun String.parseMiraiCodeImpl(contact: Contact?): MessageChain = buildMessageChain {
    forEachMiraiCode { origin, name, args ->
        if (name == null) {
            add(PlainText(origin.decodeMiraiCode()))
            return@forEachMiraiCode
        }
        val parser = MiraiCodeParsers[name] ?: kotlin.run {
            add(PlainText(origin.decodeMiraiCode()))
            return@forEachMiraiCode
        }
        parser.parse(contact, args)
            ?.let(::add)
            ?: add(PlainText(origin.decodeMiraiCode()))
    }
}

private fun String.forEachMiraiCode(block: (origin: String, name: String?, args: String) -> Unit) {
    var pos = 0
    var lastPos = 0
    val len = length - 7 // [mirai:
    fun findEnding(start: Int): Int {
        var pos0 = start
        while (pos0 < length) {
            when (get(pos0)) {
                '\\' -> pos0 += 2
                ']' -> return pos0
                else -> pos0++
            }
        }
        return -1
    }
    while (pos < len) {
        when (get(pos)) {
            '\\' -> {
                pos += 2
            }
            '[' -> {
                if (get(pos + 1) == 'm' && get(pos + 2) == 'i' &&
                    get(pos + 3) == 'r' && get(pos + 4) == 'a' &&
                    get(pos + 5) == 'i' && get(pos + 6) == ':'
                ) {
                    val begin = pos
                    pos += 7
                    val ending = findEnding(pos)
                    if (ending == -1) {
                        block(substring(lastPos), null, "")
                        return
                    } else {
                        if (lastPos < begin) {
                            block(substring(lastPos, begin), null, "")
                        }
                        val v = substring(begin, ending + 1)
                        val splitter = v.indexOf(':', 7)
                        block(
                            v, if (splitter == -1)
                                v.substring(7, v.length - 1)
                            else v.substring(7, splitter),
                            if (splitter == -1) {
                                ""
                            } else v.substring(splitter + 1, v.length - 1)
                        )
                        lastPos = ending + 1
                        pos = lastPos
                    }
                } else pos++
            }
            else -> {
                pos++
            }
        }
    }
    if (lastPos < length) {
        block(substring(lastPos), null, "")
    }
}

@OptIn(MiraiInternalApi::class, MiraiExperimentalApi::class)
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
private object MiraiCodeParsers : AbstractMap<String, MiraiCodeParser>(), Map<String, MiraiCodeParser> by mapOf(
    "at" to MiraiCodeParser(Regex("""(\d*)""")) { (target) ->
        At(target.toLong())
    },
    "atall" to MiraiCodeParser(Regex("")) {
        AtAll
    },
    "poke" to MiraiCodeParser(Regex("(.*)?,(\\d*),(-?\\d*)")) { (name, type, id) ->
        PokeMessage(name, type.toInt(), id.toInt())
    },
    "vipface" to MiraiCodeParser(Regex("""(\d*),(.*),(\d*)""")) { (id, name, count) ->
        VipFace(VipFace.Kind(id.toInt(), name), count.toInt())
    },
    "face" to MiraiCodeParser(Regex("""(\d*)""")) { (id) ->
        Face(id.toInt())
    },
    "superface" to MiraiCodeParser(Regex("""(\d*),(.*),(\d*)""")) { (face, id, type) ->
        SuperFace(face.toInt(), id, type.toInt())
    },
    "image" to MiraiCodeParser(Regex("""(.*)""")) { (id) ->
        Image(id)
    },
    "flash" to MiraiCodeParser(Regex("""(.*)""")) { (id) ->
        Image(id).flash()
    },
    "service" to MiraiCodeParser(Regex("""(\d*),(.*)""")) { (id, content) ->
        SimpleServiceMessage(id.toInt(), content.decodeMiraiCode())
    },
    "app" to MiraiCodeParser(Regex("""(.*)""")) { (content) ->
        LightApp(content.decodeMiraiCode())
    },
    "dice" to MiraiCodeParser(Regex("""([1-6])""")) { (value) ->
        Dice(value.toInt())
    },
    "rps" to MiraiCodeParser(Regex("""(\w+)""")) { (value) ->
        RockPaperScissors.valueOf(value.uppercase())
    },
    "musicshare" to MiraiCodeParser.DynamicParser(7) { args ->
        val (kind, title, summary, jumpUrl, pictureUrl) = args
        val musicUrl = args[5]
        val brief = args[6]

        MusicShare(MusicKind.valueOf(kind), title, summary, jumpUrl, pictureUrl, musicUrl, brief)
    },
    "file" to MiraiCodeParser(Regex("""(.*?),(.*?),(.*?),(.*?)""")) { (id, internalId, name, size) ->
        FileMessage(id, internalId.toInt(), name, size.toLong())
    },
)


// Visitable for test
internal sealed class MiraiCodeParser {
    abstract fun parse(contact: Contact?, args: String): Message?
    class RegexParser(
        private val argsRegex: Regex,
        private val mapper: Contact?.(MatchResult.Destructured) -> Message?
    ) : MiraiCodeParser() {
        override fun parse(contact: Contact?, args: String): Message? =
            argsRegex.matchEntire(args)
                ?.destructured
                ?.let {
                    runCatching {
                        contact.mapper(it)
                    }.getOrNull()
                }
    }

    class DynamicParser(
        private val minArgs: Int,
        private val maxArgs: Int = minArgs,
        private val parser: (Contact?.(args: Array<String>) -> Message?),
    ) : MiraiCodeParser() {
        override fun parse(contact: Contact?, args: String): Message? {
            val ranges = mutableListOf<IntRange>()
            if (args.isNotEmpty()) {
                var begin = 0
                var pos = 0
                val len = args.length
                while (pos < len) {
                    when (args[pos]) {
                        '\\' -> pos += 2
                        ',' -> {
                            ranges.add(begin..pos)
                            pos++
                            begin = pos
                        }
                        else -> pos++
                    }
                }
                ranges.add(begin..len)
            }
            if (ranges.size < minArgs) return null
            if (ranges.size > maxArgs) return null
            @Suppress("RemoveExplicitTypeArguments")
            val args0 = Array<String>(ranges.size) { index ->
                val range = ranges[index]
                args.substring(range.first, range.last).decodeMiraiCode()
            }
            runCatching {
                return parser(contact, args0)
            }
            return null
        }
    }
}

private fun MiraiCodeParser(
    argsRegex: Regex,
    mapper: Contact?.(MatchResult.Destructured) -> Message?
): MiraiCodeParser = MiraiCodeParser.RegexParser(argsRegex, mapper)

internal fun StringBuilder.appendStringAsMiraiCode(value: String): StringBuilder = apply {
    value.forEach { char ->
        when (char) {
            '[', ']',
            ':', ',',
            '\\',
            -> append("\\").append(char)
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            else -> append(char)
        }
    }
}

private val DECODE_MIRAI_CODE_REGEX = """\\.""".toRegex()
private val DECODE_MIRAI_CODE_TRANSLATOR: (MatchResult) -> String = {
    when (it.value[1]) {
        'n' -> "\n"
        'r' -> "\r"
        '\n' -> ""
        else -> it.value.substring(1)
    }
}

private fun String.decodeMiraiCode() = replace(DECODE_MIRAI_CODE_REGEX, DECODE_MIRAI_CODE_TRANSLATOR)
