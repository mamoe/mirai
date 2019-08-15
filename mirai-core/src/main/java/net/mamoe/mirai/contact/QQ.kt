package net.mamoe.mirai.contact

/**
 * @author Him188moe @ Mirai Project
 */
class QQ(number: Long) : Contact(number) {
    init {
        Instances.qqs.add(this)
    }

    override fun sendMessage(message: String) {

    }

    override fun sendObjectMessage(message: String) {

    }

    /**
     * At(@) this account.
     */
    open fun at(): String {
        return "[@" + this.number + "]"
    }
}
