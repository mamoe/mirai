package net.mamoe.mirai.contact

import net.mamoe.mirai.message.Message

class Group(number: Int) : Contact(number) {
    val groupId = groupNumberToId(number)

    init {
        Instances.groups.add(this)
    }

    override fun sendMessage(message: Message) {

    }

    override fun sendXMLMessage(message: String) {

    }

    companion object {
        fun groupNumberToId(number: Int): Int {
            val left: Int = number.toString().let {
                if (it.length < 6) {
                    return@groupNumberToId number
                }
                it.substring(0, it.length - 6).toInt()
            }
            val right: Int = number.toString().let {
                it.substring(it.length - 6).toInt()
            }

            return when (left) {
                in 1..10 -> {
                    ((left + 202).toString() + right.toString()).toInt()
                }
                in 11..19 -> {
                    ((left + 469).toString() + right.toString()).toInt()
                }
                in 20..66 -> {
                    ((left + 208).toString() + right.toString()).toInt()
                }
                in 67..156 -> {
                    ((left + 1943).toString() + right.toString()).toInt()
                }
                in 157..209 -> {
                    ((left + 199).toString() + right.toString()).toInt()
                }
                in 210..309 -> {
                    ((left + 389).toString() + right.toString()).toInt()
                }
                in 310..499 -> {
                    ((left + 349).toString() + right.toString()).toInt()
                }
                else -> number
            }
        }

        fun groupIdToNumber(id: Int): Int {
            var left: Int = id.toString().let {
                if (it.length < 6) {
                    return@groupIdToNumber id
                }
                it.substring(0 until it.length - 6).toInt()
            }

            return when (left) {
                in 203..212 -> {
                    val right: Int = id.toString().let {
                        it.substring(it.length - 6).toInt()
                    }
                    ((left - 202).toString() + right.toString()).toInt()
                }
                in 480..488 -> {
                    val right: Int = id.toString().let {
                        it.substring(it.length - 6).toInt()
                    }
                    ((left - 469).toString() + right.toString()).toInt()
                }
                in 2100..2146 -> {
                    val right: Int = id.toString().let {
                        it.substring(it.length - 7).toInt()
                    }
                    left = left.toString().substring(0 until 3).toInt()
                    ((left - 208).toString() + right.toString()).toInt()
                }
                in 2010..2099 -> {
                    val right: Int = id.toString().let {
                        it.substring(it.length - 6).toInt()
                    }
                    ((left - 1943).toString() + right.toString()).toInt()
                }
                in 2147..2199 -> {
                    val right: Int = id.toString().let {
                        it.substring(it.length - 7).toInt()
                    }
                    left = left.toString().substring(0 until 3).toInt()
                    ((left - 199).toString() + right.toString()).toInt()
                }
                in 4100..4199 -> {
                    val right: Int = id.toString().let {
                        it.substring(it.length - 7).toInt()
                    }
                    left = left.toString().substring(0 until 3).toInt()
                    ((left - 389).toString() + right.toString()).toInt()
                }
                in 3800..3989 -> {
                    val right: Int = id.toString().let {
                        it.substring(it.length - 7).toInt()
                    }
                    left = left.toString().substring(0 until 3).toInt()
                    ((left - 349).toString() + right.toString()).toInt()
                }
                else -> id
            }
        }
    }
}
