/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.internal.command.fuzzySearchMember
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.console.permission.RootPermission
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*


/**
 * 使用 [String.toInt] 解析
 */
public object IntValueArgumentParser : InternalCommandValueArgumentParserExtensions<Int>() {
    public override fun parse(raw: String, sender: CommandSender): Int =
        raw.toIntOrNull() ?: illegalArgument("无法解析 $raw 为整数")
}

/**
 * 使用 [String.toLong] 解析
 */
public object LongValueArgumentParser : InternalCommandValueArgumentParserExtensions<Long>() {
    public override fun parse(raw: String, sender: CommandSender): Long =
        raw.toLongOrNull() ?: illegalArgument("无法解析 $raw 为长整数")
}

/**
 * 使用 [String.toShort] 解析
 */
public object ShortValueArgumentParser : InternalCommandValueArgumentParserExtensions<Short>() {
    public override fun parse(raw: String, sender: CommandSender): Short =
        raw.toShortOrNull() ?: illegalArgument("无法解析 $raw 为短整数")
}

/**
 * 使用 [String.toByte] 解析
 */
public object ByteValueArgumentParser : InternalCommandValueArgumentParserExtensions<Byte>() {
    public override fun parse(raw: String, sender: CommandSender): Byte =
        raw.toByteOrNull() ?: illegalArgument("无法解析 $raw 为字节")
}

/**
 * 使用 [String.toDouble] 解析
 */
public object DoubleValueArgumentParser : InternalCommandValueArgumentParserExtensions<Double>() {
    public override fun parse(raw: String, sender: CommandSender): Double =
        raw.toDoubleOrNull() ?: illegalArgument("无法解析 $raw 为小数")
}

/**
 * 使用 [String.toFloat] 解析
 */
public object FloatValueArgumentParser : InternalCommandValueArgumentParserExtensions<Float>() {
    public override fun parse(raw: String, sender: CommandSender): Float =
        raw.toFloatOrNull() ?: illegalArgument("无法解析 $raw 为小数")
}

/**
 * 直接返回 [String], 或取用 [SingleMessage.contentToString]
 */
public object StringValueArgumentParser : InternalCommandValueArgumentParserExtensions<String>() {
    public override fun parse(raw: String, sender: CommandSender): String = raw
}

/**
 * 解析 [String] 通过 [Image].
 */
public object ImageValueArgumentParser : InternalCommandValueArgumentParserExtensions<Image>() {
    public override fun parse(raw: String, sender: CommandSender): Image {
        return kotlin.runCatching {
            Image(raw)
        }.getOrElse {
            illegalArgument("无法解析 $raw 为图片.")
        }
    }

    override fun parse(raw: MessageContent, sender: CommandSender): Image {
        if (raw is Image) return raw
        return super.parse(raw, sender)
    }
}

public object PlainTextValueArgumentParser : InternalCommandValueArgumentParserExtensions<PlainText>() {
    public override fun parse(raw: String, sender: CommandSender): PlainText {
        return PlainText(raw)
    }

    override fun parse(raw: MessageContent, sender: CommandSender): PlainText {
        if (raw is PlainText) return raw
        return super.parse(raw, sender)
    }
}

/**
 * 当字符串内容为(不区分大小写) "true", "yes", "enabled", "on", "1"
 */
public object BooleanValueArgumentParser : InternalCommandValueArgumentParserExtensions<Boolean>() {
    public override fun parse(raw: String, sender: CommandSender): Boolean = raw.trim().let { str ->
        str.equals("true", ignoreCase = true)
            || str.equals("yes", ignoreCase = true)
            || str.equals("enabled", ignoreCase = true)
            || str.equals("on", ignoreCase = true)
            || str.equals("1", ignoreCase = true)
    }
}

/**
 * 根据 [Bot.id] 解析一个登录后的 [Bot]
 */
public object ExistingBotValueArgumentParser : InternalCommandValueArgumentParserExtensions<Bot>() {
    public override fun parse(raw: String, sender: CommandSender): Bot =
        if (raw == "~") sender.inferBotOrFail()
        else raw.findBotOrFail()

    public override fun parse(raw: MessageContent, sender: CommandSender): Bot =
        if (raw is At) {
            Bot.getInstanceOrNull(raw.target)
                ?: illegalArgument("@ 的对象不是一个 Bot")
        } else super.parse(raw, sender)
}

/**
 * 解析任意一个存在的好友.
 */
public object ExistingFriendValueArgumentParser : InternalCommandValueArgumentParserExtensions<Friend>() {
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

    public override fun parse(raw: MessageContent, sender: CommandSender): Friend {
        if (raw is At) {
            checkArgument(sender is MemberCommandSender)
            return sender.inferBotOrFail().getFriend(raw.target)
                ?: illegalArgument("At 的对象 ${raw.target} 非 Bot 好友")
        } else {
            illegalArgument("无法解析 $raw 为好友")
        }
    }
}

/**
 * 解析任意一个存在的群.
 */
public object ExistingGroupValueArgumentParser : InternalCommandValueArgumentParserExtensions<Group>() {
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

public object ExistingUserValueArgumentParser : InternalCommandValueArgumentParserExtensions<User>() {
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
        return parseImpl(sender, raw, ExistingMemberValueArgumentParser::parse, ExistingFriendValueArgumentParser::parse)
    }

    override fun parse(raw: MessageContent, sender: CommandSender): User {
        return parseImpl(sender, raw, ExistingMemberValueArgumentParser::parse, ExistingFriendValueArgumentParser::parse)
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


public object ExistingContactValueArgumentParser : InternalCommandValueArgumentParserExtensions<Contact>() {
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
        return parseImpl(sender, raw, ExistingUserValueArgumentParser::parse, ExistingGroupValueArgumentParser::parse)
    }

    override fun parse(raw: MessageContent, sender: CommandSender): Contact {
        return parseImpl(sender, raw, ExistingUserValueArgumentParser::parse, ExistingGroupValueArgumentParser::parse)
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
public object ExistingMemberValueArgumentParser : InternalCommandValueArgumentParserExtensions<Member>() {
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

    public override fun parse(raw: MessageContent, sender: CommandSender): Member {
        return if (raw is At) {
            checkArgument(sender is MemberCommandSender)
            val bot = sender.inferBotOrFail()
            val group = sender.inferGroupOrFail()
            if (raw.target == bot.id) {
                return group.botAsMember
            }
            group[raw.target] ?: illegalArgument("无法找到群员 ${raw.target}")
        } else {
            parse(raw.content, sender)
        }
    }
}

public object PermissionIdValueArgumentParser : InternalCommandValueArgumentParserExtensions<PermissionId>() {
    override fun parse(raw: String, sender: CommandSender): PermissionId {
        return kotlin.runCatching { PermissionId.parseFromString(raw) }.getOrElse {
            if (raw == "*") return RootPermission.id // for convenience
            illegalArgument("无法解析 $raw 为权限 ID.")
        }
    }
}

public object PermitteeIdValueArgumentParser : InternalCommandValueArgumentParserExtensions<PermitteeId>() {
    override fun parse(raw: String, sender: CommandSender): PermitteeId {
        return if (raw == "~") sender.permitteeId
        else {
            kotlin.runCatching { AbstractPermitteeId.parseFromString(raw) }.getOrElse {
                val long = raw.toLongOrNull()
                if (long != null) {
                    return AbstractPermitteeId.ExactUser(long)// for convenience
                }

                illegalArgument("无法解析 $raw 为被许可人 ID.")
            }
        }
    }

    override fun parse(raw: MessageContent, sender: CommandSender): PermitteeId {
        if (raw is At) {
            return ExistingUserValueArgumentParser.parse(raw, sender).asCommandSender(false).permitteeId
        }
        return super.parse(raw, sender)
    }
}

/** 直接返回原始参数 [MessageContent] */
public object RawContentValueArgumentParser : CommandValueArgumentParser<MessageContent> {
    override fun parse(raw: String, sender: CommandSender): MessageContent = PlainText(raw)
    override fun parse(raw: MessageContent, sender: CommandSender): MessageContent = raw
}

/**
 * 解析参数为枚举 [T]
 *
 * 注:
 * - 当枚举值大小写无冲突时会尝试忽略大小写
 * - 当大小写驼峰可用时会尝试使用大小写驼峰
 *
 * 例如:
 * ```
 * enum class StdType { STD_IN, STD_OUT, STD_ERR }
 * ```
 * 对于 StdType 有以下值可用:
 * - `STD_IN`, `STD_OUT`, `STD_ERR`  (忽视大小写)
 * - `stdIn`,  `stdOut`,  `stdErr`   (不忽视大小写)
 *
 * @since 2.2
 */
public class EnumValueArgumentParser<T : Enum<T>>(
    private val type: Class<T>,
) : InternalCommandValueArgumentParserExtensions<T>() {
    // 此 Exception 仅用于中断 enum 搜索, 不需要使用堆栈信息
    private object NoEnumException : RuntimeException()


    init {
        check(Enum::class.java.isAssignableFrom(type)) {
            "$type not a enum class"
        }
    }

    private fun <T> Sequence<T>.hasDuplicates(): Boolean = iterator().hasDuplicates()
    private fun <T> Iterator<T>.hasDuplicates(): Boolean {
        val observed = HashSet<T>()
        for (elem in this) {
            if (!observed.add(elem))
                return true
        }
        return false
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun noConstant(): Nothing {
        throw NoEnumException
    }

    private val delegate: (String) -> T = kotlin.run {
        val enums = type.enumConstants.asSequence()
        // step 1: 分析是否能够忽略大小写
        if (enums.map { it.name.lowercase() }.hasDuplicates()) {
            ({ java.lang.Enum.valueOf(type, it) })
        } else { // step 2: 分析是否能使用小驼峰命名
            val lowerCaseEnumDirection = enums.map { it.name.lowercase() to it }.toList().toMap()

            val camelCase = enums.mapNotNull { elm ->
                val name = elm.name.split('_')
                if (name.size == 1) { // No splitter
                    null
                } else {
                    buildString {
                        val iterator = name.iterator()
                        append(iterator.next().lowercase())
                        for (v in iterator) {
                            if (v.isEmpty()) continue
                            append(v[0].uppercase())
                            append(v.substring(1, v.length).lowercase())
                        }
                    } to elm
                }
            }

            val camelCaseDirection = if ((
                    enums.map { it.name.lowercase() } + camelCase.map { it.first.lowercase() }
                    ).hasDuplicates()
            ) { // 确认驼峰命名与源没有冲突
                emptyMap()
            } else {
                camelCase.toList().toMap()
            }

            ({
                camelCaseDirection[it]
                    ?: lowerCaseEnumDirection[it.lowercase()]
                    ?: noConstant()
            })
        }
    }

    override fun parse(raw: String, sender: CommandSender): T {
        return try {
            delegate(raw)
        } catch (e: Throwable) {
            illegalArgument("无法解析 $raw 为 ${type.simpleName}")
        }
    }
}

internal abstract class InternalCommandValueArgumentParserExtensions<T : Any> : AbstractCommandValueArgumentParser<T>() {
    private fun String.parseToLongOrFail(): Long = toLongOrNull() ?: illegalArgument("无法解析 $this 为整数")

    protected fun Long.findBotOrFail(): Bot = Bot.getInstanceOrNull(this) ?: illegalArgument("无法找到 Bot: $this")

    protected fun String.findBotOrFail(): Bot =
        Bot.getInstanceOrNull(this.parseToLongOrFail()) ?: illegalArgument("无法找到 Bot: $this")

    protected fun Bot.findGroupOrFail(id: Long): Group = getGroup(id) ?: illegalArgument("无法找到群: $this")

    protected fun Bot.findGroupOrFail(id: String): Group =
        getGroup(id.parseToLongOrFail()) ?: illegalArgument("无法找到群: $this")

    protected fun Bot.findFriendOrFail(id: String): Friend =
        getFriend(id.parseToLongOrFail()) ?: illegalArgument("无法找到好友: $this")

    protected fun Bot.findMemberOrFail(id: String): Friend =
        getFriend(id.parseToLongOrFail()) ?: illegalArgument("无法找到群员: $this")

    protected fun Group.findMemberOrFail(idOrCard: String): Member {
        if (idOrCard == "\$") return members.randomOrNull() ?: illegalArgument("当前语境下无法推断随机群员")
        idOrCard.toLongOrNull()?.let { get(it) }?.let { return it }
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

    protected fun CommandSender.inferBotOrFail(): Bot =
        (this as? UserCommandSender)?.bot
            ?: Bot.instancesSequence.singleOrNull()
            ?: illegalArgument("当前语境下无法推断目标 Bot, 因为目前有多个 Bot 在线.")

    protected fun CommandSender.inferGroupOrFail(): Group =
        inferGroup() ?: illegalArgument("当前语境下无法推断目标群")

    protected fun CommandSender.inferGroup(): Group? = (this as? GroupAwareCommandSender)?.group

    protected fun CommandSender.inferFriendOrFail(): Friend =
        (this as? FriendCommandSender)?.user ?: illegalArgument("当前语境下无法推断目标好友")
}

internal fun Double.toDecimalPlace(n: Int): String = "%.${n}f".format(this)

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