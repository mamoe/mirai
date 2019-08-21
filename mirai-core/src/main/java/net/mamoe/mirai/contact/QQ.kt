package net.mamoe.mirai.contact

/**
 * @author Him188moe
 */
class QQ(number: Int) : Contact(number) {
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
    fun at(): String {
        return "[@$number]"
    }


    /*
    Make that we can use (QQ + QQ2 + QQ3).sendMessage( )

    operator fun plus(qq: QQ): QQCombination {

    }*/
}
