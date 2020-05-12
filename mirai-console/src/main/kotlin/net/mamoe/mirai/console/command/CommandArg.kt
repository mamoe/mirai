package net.mamoe.mirai.console.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.utils.fuzzySearchMember
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.content
import java.lang.NumberFormatException

/**
 * this output type of that arg
 * input is always String
 */
interface CommandArg<T:Any>{
    operator fun invoke(s:String, commandSender: CommandSender):T = parse(s,commandSender)

    operator fun invoke(s:SingleMessage, commandSender: CommandSender):T = parse(s,commandSender)

    fun parse(s:String, commandSender: CommandSender):T

    fun parse(s:SingleMessage, commandSender: CommandSender):T
}


abstract class CommandArgImpl<T:Any>(): CommandArg<T> {
    override fun parse(s: SingleMessage, commandSender: CommandSender): T  = parse(s.content,commandSender)
}

class IntArg: CommandArgImpl<Int>(){
    override fun parse(s: String, commandSender: CommandSender): Int {
        return try{
            s.toInt()
        }catch (e:Exception){
            error("无法识别整数$s")
        }
    }
}

class LongArg: CommandArgImpl<Long>(){
    override fun parse(s: String, commandSender: CommandSender): Long {
        return try{
            s.toLong()
        }catch (e:Exception){
            error("无法识别长整数$s")
        }
    }
}

class DoubleArg: CommandArgImpl<Double>(){
    override fun parse(s: String, commandSender: CommandSender): Double {
        return try{
            s.toDouble()
        }catch (e:Exception){
            error("无法识别小数$s")
        }
    }
}

class FloatArg: CommandArgImpl<Float>(){
    override fun parse(s: String, commandSender: CommandSender): Float{
        return try{
            s.toFloat()
        }catch (e:Exception){
            error("无法识别小数$s")
        }
    }
}

class BooleanArg: CommandArgImpl<Boolean>(){
    override fun parse(s: String, commandSender: CommandSender): Boolean {
        return s.equals("true",true) || s.equals("yes",true)
    }
}

class StringArg: CommandArgImpl<String>(){
    override fun parse(s: String, commandSender: CommandSender): String {
        return s
    }
}

/**
 * require a bot that already login in console
 * input: Bot UIN
 * output: Bot
 * errors: String->Int convert, Bot Not Exist
 */
class ExistBotArg : CommandArgImpl<Bot>() {
    override fun parse(s: String, commandSender: CommandSender): Bot {
        val uin = try {
            s.toLong()
        } catch (e: Exception) {
            error("无法识别QQ UIN$s")
        }
        return try {
            Bot.getInstance(uin)
        } catch (e: NoSuchElementException) {
            error("无法找到Bot $uin")
        }
    }
}


class ExistFriendArg: CommandArgImpl<Friend>(){
    //Bot.friend
    //friend
    //~ = self
    override fun parse(s: String, commandSender: CommandSender): Friend {
        if(s == "~"){
            if(commandSender !is BotAware){
                error("无法解析～作为默认")
            }
            val targetID = when (commandSender) {
                is GroupContactCommandSender -> commandSender.realSender.id
                is ContactCommandSender -> commandSender.contact.id
                else -> error("无法解析～作为默认")
            }
            return try{
                commandSender.bot.friends[targetID]
            }catch (e:NoSuchElementException){
                error("无法解析～作为默认")
            }
        }
        if(commandSender is BotAware){
            return try{
                commandSender.bot.friends[s.toLong()]
            }catch (e:NoSuchElementException){
                error("无法找到" + s + "这个好友")
            }catch (e:NumberFormatException){
                error("无法解析$s")
            }
        }else{
            with(s.split(".")){
                if(this.size != 2){
                    error("无法解析$s, 格式应为Bot.Friend")
                }
                return try{
                    Bot.getInstance(this[0].toLong()).friends[this[1].toLong()]
                }catch (e:NoSuchElementException){
                    error("无法找到好友或Bot")
                }catch (e:NumberFormatException){
                    error("无法解析$s")
                }
            }
        }
    }

    override fun parse(s: SingleMessage, commandSender: CommandSender): Friend {
        return if(s is At){
            assert(commandSender is GroupContactCommandSender)
            return try {
                (commandSender as BotAware).bot.friends[s.target]
            }catch (e:NoSuchElementException){
                error("At的对象非Bot好友")
            }
        }else{
            error("无法识别Member" + s.content)
        }
    }
}

class ExistGroupArg: CommandArgImpl<Group>(){
    override fun parse(s: String, commandSender: CommandSender): Group {
        //by default
        if ((s == "" || s == "~") && commandSender is GroupContactCommandSender) {
            return commandSender.contact as Group
        }
        //from bot to group
        if (commandSender is BotAware) {
            val code = try {
                s.toLong()
            } catch (e: NoSuchElementException) {
                error("无法识别Group Code$s")
            }
            return try {
                commandSender.bot.getGroup(code)
            } catch (e: NoSuchElementException) {
                error("无法找到Group " + code + " from Bot " + commandSender.bot.id)
            }
        }
        //from console/other
        return with(s.split(".")) {
            if (this.size != 2) {
                error("请使用BotQQ号.群号 来表示Bot的一个群")
            }
            try {
                Bot.getInstance(this[0].toLong()).getGroup(this[1].toLong())
            }catch (e:NoSuchElementException){
                error("无法找到" + this[0] + "的" + this[1] + "群")
            }catch (e:NumberFormatException){
                error("无法识别群号或机器人UIN")
            }
        }
    }
}

class ExistMemberArg: CommandArgImpl<Member>(){
    //后台: Bot.Group.Member[QQ/名片]
    //私聊: Group.Member[QQ/名片]
    //群内: Q号
    //群内: 名片
    override fun parse(s: String, commandSender: CommandSender): Member {
        if(commandSender !is BotAware){
            with(s.split(".")){
                if(this.size < 3){
                    error("无法识别Member, 请使用Bot.Group.Member[QQ/名片]的格式")
                }
                val bot = try {
                     Bot.getInstance(this[0].toLong())
                }catch (e:NoSuchElementException){
                    error("无法找到Bot")
                }catch (e:NumberFormatException){
                    error("无法识别Bot")
                }
                val group = try{
                    bot.getGroup(this[1].toLong())
                }catch (e:NoSuchElementException){
                    error("无法找到Group")
                }catch (e:NumberFormatException){
                    error("无法识别Group")
                }

                val memberIndex = this.subList(2,this.size).joinToString(".")
                return try{
                    group.members[memberIndex.toLong()]
                }catch (ignored:Exception){
                    group.fuzzySearchMember(memberIndex)?: error("无法找到成员$memberIndex")
                }
            }
        }else {
            val bot = commandSender.bot
            if(commandSender is GroupContactCommandSender){
                val group = commandSender.contact as Group
                return try {
                    group.members[s.toLong()]
                } catch (ignored: Exception) {
                    group.fuzzySearchMember(s) ?: error("无法找到成员$s")
                }
            }else {
                with(s.split(".")) {
                    if (this.size < 2) {
                        error("无法识别Member, 请使用Group.Member[QQ/名片]的格式")
                    }
                    val group = try {
                        bot.getGroup(this[0].toLong())
                    } catch (e: NoSuchElementException) {
                        error("无法找到Group")
                    } catch (e: NumberFormatException) {
                        error("无法识别Group")
                    }

                    val memberIndex = this.subList(1, this.size).joinToString(".")
                    return try {
                        group.members[memberIndex.toLong()]
                    } catch (ignored: Exception) {
                        group.fuzzySearchMember(memberIndex) ?: error("无法找到成员$memberIndex")
                    }
                }
            }
        }
    }

    override fun parse(s: SingleMessage, commandSender: CommandSender): Member {
        return if(s is At){
            assert(commandSender is GroupContactCommandSender)
            ((commandSender as GroupContactCommandSender).contact as Group).members[s.target]
        }else{
            error("无法识别Member" + s.content)
        }
    }
}

