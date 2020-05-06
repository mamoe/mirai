package net.mamoe.mirai.console.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.GroupContactCommandSender
import net.mamoe.mirai.contact.Group

/**
 * this output type of that arg
 * input is always String
 */
interface CommandArg<T:Any>{
    operator fun invoke():T = get()

    fun get():T

    fun read(s:String, commandSender: CommandSender)
}


abstract class CommandArgImpl<T:Any>(
):CommandArg<T>{

    lateinit var value:T

    override fun get(): T = value

    override fun read(s: String, commandSender: CommandSender) {
        value = parse(s, commandSender)
    }

    abstract fun parse(s:String, commandSender: CommandSender):T
}

class IntArg:CommandArgImpl<Int>(){
    override fun parse(s: String, commandSender: CommandSender): Int {
        return try{
            s.toInt()
        }catch (e:Exception){
            error("无法识别整数$s")
        }
    }
}

class LongArg:CommandArgImpl<Long>(){
    override fun parse(s: String, commandSender: CommandSender): Long {
        return try{
            s.toLong()
        }catch (e:Exception){
            error("无法识别长整数$s")
        }
    }
}

class DoubleArg:CommandArgImpl<Double>(){
    override fun parse(s: String, commandSender: CommandSender): Double {
        return try{
            s.toDouble()
        }catch (e:Exception){
            error("无法识别小数$s")
        }
    }
}


class StringArg:CommandArgImpl<String>(){
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

class ExistBotArg:CommandArgImpl<Bot>(){
    override fun parse(s: String, commandSender: CommandSender): Bot {
        val uin = try{
            s.toLong()
        }catch (e:Exception){
            error("无法识别QQ UIN$s")
        }
        return try{
            Bot.getInstance(uin)
        }catch (e:NoSuchElementException){
            error("无法找到Bot $uin")
        }
    }
}



class ExistGroupArg:CommandArgImpl<Group>(){

    override fun parse(s: String, commandSender: CommandSender): Group {
        if((s === "" || s === "~") && commandSender is GroupContactCommandSender){
            return commandSender.contact as Group
        }

        val code = try{
            s.toLong()
        }catch (e:Exception){
            error("无法识别Group Code$s")
        }

        TODO()

    }
}
