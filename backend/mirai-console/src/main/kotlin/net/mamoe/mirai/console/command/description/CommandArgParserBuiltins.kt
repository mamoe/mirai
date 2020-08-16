/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.description

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.BotAwareCommandSender
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.internal.command.fuzzySearchMember
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.content


public object IntArgumentParser : CommandArgumentParser<Int> {
    public override fun parse(raw: String, sender: CommandSender): Int =
        raw.toIntOrNull() ?: illegalArgument("无法解析 $raw 为整数")
}

public object LongArgumentParser : CommandArgumentParser<Long> {
    public override fun parse(raw: String, sender: CommandSender): Long =
        raw.toLongOrNull() ?: illegalArgument("无法解析 $raw 为长整数")
}

public object ShortArgumentParser : CommandArgumentParser<Short> {
    public override fun parse(raw: String, sender: CommandSender): Short =
        raw.toShortOrNull() ?: illegalArgument("无法解析 $raw 为短整数")
}

public object ByteArgumentParser : CommandArgumentParser<Byte> {
    public override fun parse(raw: String, sender: CommandSender): Byte =
        raw.toByteOrNull() ?: illegalArgument("无法解析 $raw 为字节")
}

public object DoubleArgumentParser : CommandArgumentParser<Double> {
    public override fun parse(raw: String, sender: CommandSender): Double =
        raw.toDoubleOrNull() ?: illegalArgument("无法解析 $raw 为小数")
}

public object FloatArgumentParser : CommandArgumentParser<Float> {
    public override fun parse(raw: String, sender: CommandSender): Float =
        raw.toFloatOrNull() ?: illegalArgument("无法解析 $raw 为小数")
}

public object StringArgumentParser : CommandArgumentParser<String> {
    public override fun parse(raw: String, sender: CommandSender): String {
        return raw
    }
}

public object BooleanArgumentParser : CommandArgumentParser<Boolean> {
    public override fun parse(raw: String, sender: CommandSender): Boolean = raw.trim().let { str ->
        str.equals("true", ignoreCase = true)
                || str.equals("yes", ignoreCase = true)
                || str.equals("enabled", ignoreCase = true)
    }
}

/**
 * require a bot that already login in console
 * input: Bot UIN
 * output: Bot
 * errors: String->Int convert, Bot Not Exist
 */
public object ExistBotArgumentParser : CommandArgumentParser<Bot> {
    public override fun parse(raw: String, sender: CommandSender): Bot {
        val uin = raw.toLongOrNull() ?: illegalArgument("无法识别 QQ ID: $raw")
        return Bot.getInstanceOrNull(uin) ?: illegalArgument("无法找到 Bot $uin")
    }
}

public object ExistFriendArgumentParser : CommandArgumentParser<Friend> {
    //Bot.friend
    //friend
    //~ = self
    public override fun parse(raw: String, sender: CommandSender): Friend {
        if (raw == "~") {
            if (sender !is BotAwareCommandSender) {
                illegalArgument("无法解析～作为默认")
            }
            val targetID = when (sender) {
                is UserCommandSender -> sender.user.id
                else -> illegalArgument("无法解析～作为默认")
            }

            return sender.bot.friends.getOrNull(targetID) ?: illegalArgument("无法解析～作为默认")
        }
        if (sender is BotAwareCommandSender) {
            return sender.bot.friends.getOrNull(raw.toLongOrNull() ?: illegalArgument("无法解析 $raw 为整数"))
                ?: illegalArgument("无法找到" + raw + "这个好友")
        } else {
            raw.split(".").let { args ->
                if (args.size != 2) {
                    illegalArgument("无法解析 $raw, 格式应为 机器人账号.好友账号")
                }
                return try {
                    Bot.getInstance(args[0].toLong()).friends.getOrNull(
                        args[1].toLongOrNull() ?: illegalArgument("无法解析 $raw 为好友")
                    ) ?: illegalArgument("无法找到好友 ${args[1]}")
                } catch (e: NoSuchElementException) {
                    illegalArgument("无法找到机器人账号 ${args[0]}")
                }
            }
        }
    }

    public override fun parse(raw: SingleMessage, sender: CommandSender): Friend {
        if (raw is At) {
            assert(sender is MemberCommandSender)
            return (sender as BotAwareCommandSender).bot.friends.getOrNull(raw.target) ?: illegalArgument("At的对象非Bot好友")
        } else {
            illegalArgument("无法解析 $raw 为好友")
        }
    }
}

public object ExistGroupArgumentParser : CommandArgumentParser<Group> {
    public override fun parse(raw: String, sender: CommandSender): Group {
        //by default
        if ((raw == "" || raw == "~") && sender is MemberCommandSender) {
            return sender.group
        }
        //from bot to group
        if (sender is BotAwareCommandSender) {
            val code = try {
                raw.toLong()
            } catch (e: NoSuchElementException) {
                illegalArgument("无法识别Group Code$raw")
            }
            return try {
                sender.bot.getGroup(code)
            } catch (e: NoSuchElementException) {
                illegalArgument("无法找到Group " + code + " from Bot " + sender.bot.id)
            }
        }
        //from console/other
        return with(raw.split(".")) {
            if (this.size != 2) {
                illegalArgument("请使用BotQQ号.群号 来表示Bot的一个群")
            }
            try {
                Bot.getInstance(this[0].toLong()).getGroup(this[1].toLong())
            } catch (e: NoSuchElementException) {
                illegalArgument("无法找到" + this[0] + "的" + this[1] + "群")
            } catch (e: NumberFormatException) {
                illegalArgument("无法识别群号或机器人UIN")
            }
        }
    }
}

public object ExistMemberArgumentParser : CommandArgumentParser<Member> {
    //后台: Bot.Group.Member[QQ/名片]
    //私聊: Group.Member[QQ/名片]
    //群内: Q号
    //群内: 名片
    public override fun parse(raw: String, sender: CommandSender): Member {
        if (sender !is BotAwareCommandSender) {
            with(raw.split(".")) {
                checkArgument(this.size >= 3) {
                    "无法识别Member, 请使用Bot.Group.Member[QQ/名片]的格式"
                }

                val bot = try {
                    Bot.getInstance(this[0].toLong())
                } catch (e: NoSuchElementException) {
                    illegalArgument("无法找到Bot")
                } catch (e: NumberFormatException) {
                    illegalArgument("无法识别Bot")
                }

                val group = try {
                    bot.getGroup(this[1].toLong())
                } catch (e: NoSuchElementException) {
                    illegalArgument("无法找到Group")
                } catch (e: NumberFormatException) {
                    illegalArgument("无法识别Group")
                }

                val memberIndex = this.subList(2, this.size).joinToString(".")
                return group.members.getOrNull(memberIndex.toLong())
                    ?: group.fuzzySearchMember(memberIndex)
                    ?: error("无法找到成员$memberIndex")
            }
        } else {
            val bot = sender.bot
            if (sender is MemberCommandSender) {
                val group = sender.group
                return try {
                    group.members[raw.toLong()]
                } catch (ignored: Exception) {
                    group.fuzzySearchMember(raw) ?: illegalArgument("无法找到成员$raw")
                }
            } else {
                with(raw.split(".")) {
                    if (this.size < 2) {
                        illegalArgument("无法识别Member, 请使用Group.Member[QQ/名片]的格式")
                    }
                    val group = try {
                        bot.getGroup(this[0].toLong())
                    } catch (e: NoSuchElementException) {
                        illegalArgument("无法找到Group")
                    } catch (e: NumberFormatException) {
                        illegalArgument("无法识别Group")
                    }

                    val memberIndex = this.subList(1, this.size).joinToString(".")
                    return try {
                        group.members[memberIndex.toLong()]
                    } catch (ignored: Exception) {
                        group.fuzzySearchMember(memberIndex) ?: illegalArgument("无法找到成员$memberIndex")
                    }
                }
            }
        }
    }

    public override fun parse(raw: SingleMessage, sender: CommandSender): Member {
        return if (raw is At) {
            checkArgument(sender is MemberCommandSender)
            (sender.group).members[raw.target]
        } else {
            illegalArgument("无法识别Member" + raw.content)
        }
    }
}

