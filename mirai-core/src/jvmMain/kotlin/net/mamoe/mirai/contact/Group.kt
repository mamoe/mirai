package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group.Companion.groupNumberToId
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.utils.ContactList
import java.io.Closeable

/**
 * 群.
 *
 * Group ID 与 Group Number 并不是同一个值.
 * - Group Number([Group.number]) 是通常使用的群号码.(在 QQ 客户端中可见)
 * - Group ID([Group.groupId]) 是与服务器通讯时使用的 id.(在 QQ 客户端中不可见)
 *
 * Java 获取 groupNumber: `group.getNumber()`
 * Java 获取所属 bot: `group.getBot()`
 * Java 获取群成员列表: `group.getMembers()`
 * Java 获取 groupId: `group.getGroupId()`
 *
 * Java 调用 [groupNumberToId] : `Group.groupNumberToId(number)`
 * @author Him188moe
 */
class Group(bot: Bot, number: Long) : Contact(bot, number), Closeable {
    val groupId = groupNumberToId(number)
    val members = ContactList<QQ>()//todo members

    override suspend fun sendMessage(message: MessageChain) {
        bot.network.message.sendGroupMessage(this, message)
    }

    override suspend fun sendXMLMessage(message: String) {

    }

    override fun close() {
        this.members.clear()
    }

    companion object {
        @JvmStatic
        fun groupNumberToId(number: Long): Long {//求你别出错
            val left: Long = number.toString().let {
                if (it.length < 6) {
                    return@groupNumberToId number
                }
                it.substring(0, it.length - 6).toLong()
            }
            val right: Long = number.toString().let {
                it.substring(it.length - 6).toLong()
            }

            return when (left) {
                in 1..10 -> {
                    ((left + 202).toString() + right.toString()).toLong()
                }
                in 11..19 -> {
                    ((left + 469).toString() + right.toString()).toLong()
                }
                in 20..66 -> {
                    ((left + 208).toString() + right.toString()).toLong()
                }
                in 67..156 -> {
                    ((left + 1943).toString() + right.toString()).toLong()
                }
                in 157..209 -> {
                    ((left + 199).toString() + right.toString()).toLong()
                }
                in 210..309 -> {
                    ((left + 389).toString() + right.toString()).toLong()
                }
                in 310..499 -> {
                    ((left + 349).toString() + right.toString()).toLong()
                }
                else -> number
            }
        }

        @JvmStatic
        fun groupIdToNumber(id: Long): Long {//求你别出错
            var left: Long = id.toString().let {
                if (it.length < 6) {
                    return@groupIdToNumber id
                }
                it.substring(0 until it.length - 6).toLong()
            }

            return when (left) {
                in 203..212 -> {
                    val right: Long = id.toString().let {
                        it.substring(it.length - 6).toLong()
                    }
                    ((left - 202).toString() + right.toString()).toLong()
                }
                in 480..488 -> {
                    val right: Long = id.toString().let {
                        it.substring(it.length - 6).toLong()
                    }
                    ((left - 469).toString() + right.toString()).toLong()
                }
                in 2100..2146 -> {
                    val right: Long = id.toString().let {
                        it.substring(it.length - 7).toLong()
                    }
                    left = left.toString().substring(0 until 3).toLong()
                    ((left - 208).toString() + right.toString()).toLong()
                }
                in 2010..2099 -> {
                    val right: Long = id.toString().let {
                        it.substring(it.length - 6).toLong()
                    }
                    ((left - 1943).toString() + right.toString()).toLong()
                }
                in 2147..2199 -> {
                    val right: Long = id.toString().let {
                        it.substring(it.length - 7).toLong()
                    }
                    left = left.toString().substring(0 until 3).toLong()
                    ((left - 199).toString() + right.toString()).toLong()
                }
                in 4100..4199 -> {
                    val right: Long = id.toString().let {
                        it.substring(it.length - 7).toLong()
                    }
                    left = left.toString().substring(0 until 3).toLong()
                    ((left - 389).toString() + right.toString()).toLong()
                }
                in 3800..3989 -> {
                    val right: Long = id.toString().let {
                        it.substring(it.length - 7).toLong()
                    }
                    left = left.toString().substring(0 until 3).toLong()
                    ((left - 349).toString() + right.toString()).toLong()
                }
                else -> id
            }
        }
    }
}
