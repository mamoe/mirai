/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.description

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.internal.command.fuzzySearchMember
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.content


/**
 * 使用 [String.toInt] 解析
 */
public object IntArgumentParser : InternalCommandArgumentParserExtensions<Int> {
    public override fun parse(raw: String, sender: CommandSender): Int =
        raw.toIntOrNull() ?: illegalArgument("无法解析 $raw 为整数")
}

/**
 * 使用 [String.toInt] 解析
 */
public object LongArgumentParser : InternalCommandArgumentParserExtensions<Long> {
    public override fun parse(raw: String, sender: CommandSender): Long =
        raw.toLongOrNull() ?: illegalArgument("无法解析 $raw 为长整数")
}

/**
 * 使用 [String.toInt] 解析
 */
public object ShortArgumentParser : InternalCommandArgumentParserExtensions<Short> {
    public override fun parse(raw: String, sender: CommandSender): Short =
        raw.toShortOrNull() ?: illegalArgument("无法解析 $raw 为短整数")
}

/**
 * 使用 [String.toInt] 解析
 */
public object ByteArgumentParser : InternalCommandArgumentParserExtensions<Byte> {
    public override fun parse(raw: String, sender: CommandSender): Byte =
        raw.toByteOrNull() ?: illegalArgument("无法解析 $raw 为字节")
}

/**
 * 使用 [String.toInt] 解析
 */
public object DoubleArgumentParser : InternalCommandArgumentParserExtensions<Double> {
    public override fun parse(raw: String, sender: CommandSender): Double =
        raw.toDoubleOrNull() ?: illegalArgument("无法解析 $raw 为小数")
}

/**
 * 使用 [String.toInt] 解析
 */
public object FloatArgumentParser : InternalCommandArgumentParserExtensions<Float> {
    public override fun parse(raw: String, sender: CommandSender): Float =
        raw.toFloatOrNull() ?: illegalArgument("无法解析 $raw 为小数")
}

/**
 * 直接返回 [String], 或取用 [SingleMessage.contentToString]
 */
public object StringArgumentParser : InternalCommandArgumentParserExtensions<String> {
    public override fun parse(raw: String, sender: CommandSender): String = raw
}

/**
 * 当字符串内容为(不区分大小写) "true", "yes", "enabled"
 */
public object BooleanArgumentParser : InternalCommandArgumentParserExtensions<Boolean> {
    public override fun parse(raw: String, sender: CommandSender): Boolean = raw.trim().let { str ->
        str.equals("true", ignoreCase = true)
                || str.equals("yes", ignoreCase = true)
                || str.equals("enabled", ignoreCase = true)
                || str.equals("on", ignoreCase = true)
    }
}

/**
 * 根据 [Bot.id] 解析一个登录后的 [Bot]
 */
public object ExistingBotArgumentParser : InternalCommandArgumentParserExtensions<Bot> {
    public override fun parse(raw: String, sender: CommandSender): Bot =
        if (raw == "~") sender.inferBotOrFail()
        else raw.findBotOrFail()

    public override fun parse(raw: SingleMessage, sender: CommandSender): Bot =
        if (raw is At) {
            Bot.getInstanceOrNull(raw.target)
                ?: illegalArgument("@ 的对象不是一个 Bot")
        } else super.parse(raw, sender)
}

/**
 * 解析任意一个存在的好友.
 */
public object ExistingFriendArgumentParser : InternalCommandArgumentParserExtensions<Friend> {
    private val syntax = """
        - `botId.friendId`
        - `botId.friendNick` (模糊搜索, 寻找最优匹配)
        - `~` (指代指令调用人自己作为好友. 仅聊天环境下)
        
        当只登录了一个 [Bot] 时, `botId` 参数可省略
    """.trimIndent()

    public override fun parse(raw: String, sender: CommandSender): Friend {
        if (raw == "~") return sender.inferFriendOrFail()

        val components = raw.split(".")

        return when (components.size) {
            2 -> components.let { (botId, groupId) ->
                botId.findBotOrFail().findFriendOrFail(groupId)
            }
            1 -> components.let { (groupId) ->
                sender.inferBotOrFail().findFriendOrFail(groupId)
            }
            else -> illegalArgument("好友语法错误. \n${syntax}")
        }
    }

    public override fun parse(raw: SingleMessage, sender: CommandSender): Friend {
        if (raw is At) {
            checkArgument(sender is MemberCommandSender)
            return sender.inferBotOrFail().getFriendOrNull(raw.target)
                ?: illegalArgument("At 的对象 ${raw.target} 非 Bot 好友")
        } else {
            illegalArgument("无法解析 $raw 为好友")
        }
    }
}

/**
 * 解析任意一个存在的群.
 */
public object ExistingGroupArgumentParser : InternalCommandArgumentParserExtensions<Group> {
    private val syntax = """
        - `botId.groupId`
        - `~` (指代指令调用人自己所在群. 仅群聊天环境下)
        当只登录了一个 [Bot] 时, `botId` 参数可省略
    """.trimIndent()

    public override fun parse(raw: String, sender: CommandSender): Group {
        if (raw == "~") return sender.inferGroupOrFail()

        val components = raw.split(".")

        return when (components.size) {
            2 -> components.let { (botId, groupId) ->
                botId.findBotOrFail().findGroupOrFail(groupId)
            }
            1 -> components.let { (groupId) ->
                sender.inferBotOrFail().findGroupOrFail(groupId)
            }
            0 -> components.let {
                sender.inferGroupOrFail()
            }
            else -> illegalArgument("群语法错误. \n${syntax}")
        }
    }
}

public object ExistingUserArgumentParser : InternalCommandArgumentParserExtensions<User> {
    private val syntax: String = """
         - `botId.groupId.memberId`
         - `botId.groupId.memberCard` (模糊搜索, 寻找最优匹配)
         - `~` (指代指令调用人自己. 仅聊天环境下)
         - `botId.groupId.$` (随机成员. )
         - `botId.friendId
         
         当处于一个群内时, `botId` 和 `groupId` 参数都可省略
         当只登录了一个 [Bot] 时, `botId` 参数可省略
    """.trimIndent()

    override fun parse(raw: String, sender: CommandSender): User {
        return parseImpl(sender, raw, ExistingMemberArgumentParser::parse, ExistingFriendArgumentParser::parse)
    }

    override fun parse(raw: SingleMessage, sender: CommandSender): User {
        return parseImpl(sender, raw, ExistingMemberArgumentParser::parse, ExistingFriendArgumentParser::parse)
    }

    private fun <T> parseImpl(
        sender: CommandSender,
        raw: T,
        parseFunction: (T, CommandSender) -> User,
        parseFunction2: (T, CommandSender) -> User,
    ): User {
        if (sender.inferGroup() != null) {
            kotlin.runCatching {
                return parseFunction(raw, sender)
            }.recoverCatching {
                return parseFunction2(raw, sender)
            }.getOrElse {
                illegalArgument("无法推断目标好友或群员. \n$syntax")
            }
        }
        kotlin.runCatching {
            return parseFunction2(raw, sender)
        }.getOrElse {
            illegalArgument("无法推断目标好友或群员. \n$syntax")
        }
    }
}


public object ExistingContactArgumentParser : InternalCommandArgumentParserExtensions<Contact> {
    private val syntax: String = """
         - `botId.groupId.memberId`
         - `botId.groupId.memberCard` (模糊搜索, 寻找最优匹配)
         - `botId.groupId.$` (随机成员. 仅聊天环境下)
         - `botId.friendId
         - `botId.groupId`
         
         当处于一个群内时, `botId` 和 `groupId` 参数都可省略
         当只登录了一个 [Bot] 时, `botId` 参数可省略
    """.trimIndent()

    override fun parse(raw: String, sender: CommandSender): Contact {
        return parseImpl(sender, raw, ExistingUserArgumentParser::parse, ExistingGroupArgumentParser::parse)
    }

    override fun parse(raw: SingleMessage, sender: CommandSender): Contact {
        return parseImpl(sender, raw, ExistingUserArgumentParser::parse, ExistingGroupArgumentParser::parse)
    }

    private fun <T> parseImpl(
        sender: CommandSender,
        raw: T,
        parseFunction: (T, CommandSender) -> Contact,
        parseFunction2: (T, CommandSender) -> Contact,
    ): Contact {
        kotlin.runCatching {
            return parseFunction(raw, sender)
        }.recoverCatching {
            return parseFunction2(raw, sender)
        }.getOrElse {
            illegalArgument("无法推断目标好友, 群或群员. \n$syntax")
        }
    }
}


/**
 * 解析任意一个群成员.
 */
public object ExistingMemberArgumentParser : InternalCommandArgumentParserExtensions<Member> {
    private val syntax: String = """
         - `botId.groupId.memberId`
         - `botId.groupId.memberCard` (模糊搜索, 寻找最优匹配)
         - `~` (指代指令调用人自己. 仅聊天环境下)
         - `groupId.$` (随机成员)
         
         当处于一个群内时, `botId` 和 `groupId` 参数都可省略
         当只登录了一个 [Bot] 时, `botId` 参数可省略
    """.trimIndent()

    public override fun parse(raw: String, sender: CommandSender): Member {
        if (raw == "~") return (sender as? MemberCommandSender)?.user ?: illegalArgument("当前语境下无法推断自身作为群员")
        if (raw == "\$") return (sender as? MemberCommandSender)?.group?.members?.randomOrNull()
            ?: illegalArgument("当前语境下无法推断随机群员")

        val components = raw.split(".")

        return when (components.size) {
            3 -> components.let { (botId, groupId, memberIdOrCard) ->
                botId.findBotOrFail().findGroupOrFail(groupId).findMemberOrFail(memberIdOrCard)
            }
            2 -> components.let { (groupId, memberIdOrCard) ->
                sender.inferBotOrFail().findGroupOrFail(groupId).findMemberOrFail(memberIdOrCard)
            }
            1 -> components.let { (memberIdOrCard) ->
                sender.inferGroupOrFail().findMemberOrFail(memberIdOrCard)
            }
            else -> illegalArgument("群成员语法错误. \n$syntax")
        }
    }

    public override fun parse(raw: SingleMessage, sender: CommandSender): Member {
        return if (raw is At) {
            checkArgument(sender is MemberCommandSender)
            val bot = sender.inferBotOrFail()
            val group = sender.inferGroupOrFail()
            if (raw.target == bot.id) {
                return group.botAsMember
            }
            group[raw.target]
        } else {
            parse(raw.content, sender)
        }
    }
}

internal interface InternalCommandArgumentParserExtensions<T : Any> : CommandArgumentParser<T> {
    fun String.parseToLongOrFail(): Long = toLongOrNull() ?: illegalArgument("无法解析 $this 为整数")

    fun Long.findBotOrFail(): Bot = Bot.getInstanceOrNull(this) ?: illegalArgument("无法找到 Bot: $this")

    fun String.findBotOrFail(): Bot =
        Bot.getInstanceOrNull(this.parseToLongOrFail()) ?: illegalArgument("无法找到 Bot: $this")

    fun Bot.findGroupOrFail(id: Long): Group = getGroupOrNull(id) ?: illegalArgument("无法找到群: $this")

    fun Bot.findGroupOrFail(id: String): Group =
        getGroupOrNull(id.parseToLongOrFail()) ?: illegalArgument("无法找到群: $this")

    fun Bot.findFriendOrFail(id: String): Friend =
        getFriendOrNull(id.parseToLongOrFail()) ?: illegalArgument("无法找到好友: $this")

    fun Bot.findMemberOrFail(id: String): Friend =
        getFriendOrNull(id.parseToLongOrFail()) ?: illegalArgument("无法找到群员: $this")

    fun Group.findMemberOrFail(idOrCard: String): Member {
        if (idOrCard == "\$") return members.randomOrNull() ?: illegalArgument("当前语境下无法推断随机群员")
        idOrCard.toLongOrNull()?.let { getOrNull(it) }?.let { return it }
        this.members.singleOrNull { it.nameCardOrNick.contains(idOrCard) }?.let { return it }
        this.members.singleOrNull { it.nameCardOrNick.contains(idOrCard, ignoreCase = true) }?.let { return it }

        val candidates = this.fuzzySearchMember(idOrCard)
        candidates.singleOrNull()?.let {
            if (it.second == 1.0) return it.first // single match
        }
        if (candidates.isEmpty()) {
            illegalArgument("无法找到成员 $idOrCard")
        } else {
            var index = 1
            illegalArgument("无法找到成员 $idOrCard。 多个成员满足搜索结果或匹配度不足: \n\n" +
                    candidates.joinToString("\n", limit = 6) {
                        val percentage = (it.second * 100).toDecimalPlace(0)
                        "#${index++}(${percentage}%)${it.first.nameCardOrNick.truncate(10)}(${it.first.id})" // #1 15.4%
                    }
            )
        }
    }

    fun CommandSender.inferBotOrFail(): Bot =
        (this as? UserCommandSender)?.bot
            ?: Bot.botInstancesSequence.singleOrNull()
            ?: illegalArgument("当前语境下无法推断目标 Bot, 因为目前有多个 Bot 在线.")

    fun CommandSender.inferGroupOrFail(): Group =
        inferGroup() ?: illegalArgument("当前语境下无法推断目标群")

    fun CommandSender.inferGroup(): Group? = (this as? GroupAwareCommandSender)?.group

    fun CommandSender.inferFriendOrFail(): Friend =
        (this as? FriendCommandSender)?.user ?: illegalArgument("当前语境下无法推断目标好友")
}

internal fun Double.toDecimalPlace(n: Int): String {
    return "%.${n}f".format(this)
}

internal fun String.truncate(lengthLimit: Int, replacement: String = "..."): String = buildString {
    var lengthSum = 0
    for (char in this@truncate) {
        lengthSum += char.chineseLength()
        if (lengthSum > lengthLimit) {
            append(replacement)
            return toString()
        } else append(char)
    }
    return toString()
}

internal fun Char.chineseLength(): Int {
    return when (this) {
        in '\u0000'..'\u007F' -> 1
        in '\u0080'..'\u07FF' -> 2
        in '\u0800'..'\uFFFF' -> 2
        else -> 2
    }
}