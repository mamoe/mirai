package net.mamoe.mirai.contact

class Group(number: Long) : Contact(number) {
    init {
        Instances.groups.add(this)
    }

    override fun sendMessage(message: String) {

    }

    override fun sendObjectMessage(message: String) {

    }
}
