package net.mamoe.mirai.contact

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.utils.ContactList
import java.io.Closeable

class Group(number: Long) : Contact(number), Closeable {
    val groupId = groupNumberToId(number)
    val members = ContactList<QQ>()

    override fun sendMessage(message: Message) {

    }

    override fun sendXMLMessage(message: String) {

    }

    override fun close() {
        this.members.clear()
    }

    companion object {
        fun groupNumberToId(number: Long): Long {
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

        fun groupIdToNumber(id: Long): Long {
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
