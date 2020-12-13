/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER")

package net.mamoe.mirai.message.code.internal

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.*


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
        parser.argsRegex.matchEntire(args)
            ?.destructured
            ?.let {
                parser.runCatching {
                    contact.mapper(it)
                }.getOrNull()
            }
            ?.let(::add)
            ?: add(PlainText(origin.decodeMiraiCode()))
    }
}

internal fun String.forEachMiraiCode(block: (origin: String, name: String?, args: String) -> Unit) {
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

/*

@Suppress("RegExpRedundantEscape") // required on android
internal val codeRegex = Regex("""\[mirai:(.+?)(|:.*?(\\\].*?)*?)\]""")

internal inline fun String.forEachMiraiCode(crossinline block: (origin: String, name: String?, args: String) -> Unit) {
    var lastIndex = 0
    for (result in codeRegex.findAll(this)) {
        if (result.range.first != lastIndex) {
            // skipped string
            block(substring(lastIndex, result.range.first), null, "")
        }

        lastIndex = result.range.last + 1
        val id = result.groups[1]!!
        val param = result.groups[2]?.value?.substring(1) ?: ""
        block(result.value, id.value, param)
    }
    if (lastIndex != this.length) {
        block(substring(lastIndex, this.length), null, "")
    }
}
*/

internal object MiraiCodeParsers : Map<String, MiraiCodeParser> by mapOf(
    "at" to MiraiCodeParser(Regex("""(\d*)""")) { (target) ->
        (this as? Group)?.getOrNull(target.toLong())?.at()
    },
    "atall" to MiraiCodeParser(Regex("")) {
        AtAll
    },
    "poke" to MiraiCodeParser(Regex("(.*)?,(\\d*),(-?\\d*)")) { (name, type, id) ->
        // TODO
        PokeMessage(name, type.toInt(), id.toInt())
    },
    "vipface" to MiraiCodeParser(Regex("""(\d*),(.*),(\d*)""")) { (id, name, count) ->
        // TODO
        VipFace(VipFace.Kind(id.toInt(), name), count.toInt())
    },
    "face" to MiraiCodeParser(Regex("""(\d*)""")) { (id) ->
        Face(id.toInt())
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
    }
)

/*
internal object MiraiCodeParsers2 : Map<String, (args: String) -> Message?> by mapOf(
    "at" to l@{ args ->
        val group = args.split(',')
        if (group.size != 2) return@l null
        val target = group[0].toLongOrNull() ?: return@l null
        @Suppress("INVISIBLE_MEMBER")
        At(target, group[1])
    },
    "atall" to l@{
        AtAll
    },
    "poke" to l@{ args ->
        val group = args.split(',')
        if (group.size != 2) return@l null
        val type = group[1].toIntOrNull() ?: return@l null
        val id = group[2].toIntOrNull() ?: return@l null
        @Suppress("INVISIBLE_MEMBER")
        PokeMessage(group[0], type, id)
    },
    "vipface" to l@{ args ->
        val group = args.split(',')
        if (group.size != 2) return@l null
        val type = group[1].toIntOrNull() ?: return@l null
        val id = group[2].toIntOrNull() ?: return@l null
        @Suppress("INVISIBLE_MEMBER")
        PokeMessage(group[0], type, id)
    }
)*/

internal class MiraiCodeParser(
    val argsRegex: Regex,
    val mapper: Contact?.(MatchResult.Destructured) -> Message?
)

internal fun StringBuilder.appendAsMiraiCode(value: String) = apply {
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

@Suppress("RegExpRedundantEscape")
internal val DECODE_MIRAI_CODE_REGEX = """\\.""".toRegex()
internal val DECODE_MIRAI_CODE_TRANSLATOR: (MatchResult) -> String = {
    when (it.value[1]) {
        'n' -> "\n"
        'r' -> "\r"
        '\n' -> ""
        else -> it.value.substring(1)
    }
}

internal fun String.decodeMiraiCode() = replace(DECODE_MIRAI_CODE_REGEX, DECODE_MIRAI_CODE_TRANSLATOR)
