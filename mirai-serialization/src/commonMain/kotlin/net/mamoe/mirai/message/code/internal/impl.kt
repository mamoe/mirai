/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER")

package net.mamoe.mirai.message.code.internal

import net.mamoe.mirai.message.data.*


@Suppress("RegExpRedundantEscape") // required on android
internal val codeRegex = Regex("""(?:\[mirai:([^\]]*)?:(.*?)?\])|(?:\[mirai:([^:]+)\])""")

internal fun String.parseMiraiCodeImpl(): MessageChain = buildMessageChain {
    forEachMiraiCode { origin, name, args ->
        if (name == null) {
            add(origin.toMessage())
            return@forEachMiraiCode
        }
        val parser = MiraiCodeParsers[name] ?: kotlin.run {
            add(origin.toMessage())
            return@forEachMiraiCode
        }
        parser.argsRegex.matchEntire(args)
            ?.destructured
            ?.let {
                parser.runCatching {
                    mapper(it)
                }.getOrNull()
            }
            ?.let(::add)
            ?: add(origin.toMessage())
    }
}

internal inline fun String.forEachMiraiCode(crossinline block: (origin: String, name: String?, args: String) -> Unit) {
    var lastIndex = 0
    for (result in codeRegex.findAll(this)) {
        if (result.range.first != lastIndex) {
            // skipped string
            block(substring(lastIndex, result.range.first), null, "")
        }

        lastIndex = result.range.last + 1
        if (result.groups[3] != null) {
            // no param
            block(result.value, result.groups[3]!!.value, "")
        } else block(result.value, result.groups[1]!!.value, result.groups[2]?.value ?: "")
    }
}

internal object MiraiCodeParsers : Map<String, MiraiCodeParser> by mapOf(
    "at" to MiraiCodeParser(Regex("""(\d*),(.*)""")) { (target, display) ->
        At._lowLevelConstructAtInstance(target.toLong(), display)
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
    "image" to MiraiCodeParser(Regex("""(.*)""")) { (id) ->
        Image(id)
    },
    "flash" to MiraiCodeParser(Regex("""(.*)""")) { (id) ->
        Image(id).flash()
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
    val mapper: MiraiCodeParser.(MatchResult.Destructured) -> Message?
)