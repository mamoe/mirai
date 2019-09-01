package net.mamoe.mirai.contact

/**
 * A contact is a QQ account or a QQ Group.
 *
 * @author Him188moe
 */
abstract class Contact(val number: Int) {

    /**
     * Async
     */
    abstract fun sendMessage(message: String)

    /**
     * Async
     */
    abstract fun sendObjectMessage(message: String)

}
