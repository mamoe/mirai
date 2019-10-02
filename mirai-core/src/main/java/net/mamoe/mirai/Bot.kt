package net.mamoe.mirai

import net.mamoe.mirai.Bot.ContactSystem
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.utils.BotAccount
import net.mamoe.mirai.utils.ContactList
import net.mamoe.mirai.utils.MiraiLogger
import java.io.Closeable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 * <br></br>
 * [Bot] 由 3 个模块组成.
 * [联系人管理][ContactSystem]: 可通过 [Bot.contacts] 访问
 * [网络处理器][TIMBotNetworkHandler]: 可通过 [Bot.network] 访问
 * [机器人账号信息][BotAccount]: 可通过 [Bot.account] 访问
 * <br></br>
 * 若你需要得到机器人的 QQ 账号, 请访问 [Bot.account]
 * 若你需要得到服务器上所有机器人列表, 请访问 [Bot.instances]
 *
 *
 *
 * Bot that is the base of the whole program.
 * It consists of
 * a [ContactSystem], which manage contacts such as [QQ] and [Group];
 * a [TIMBotNetworkHandler], which manages the connection to the server;
 * a [BotAccount], which stores the account information(e.g. qq number the bot)
 * <br></br>
 * To of all the QQ contacts, access [Bot.account]
 * To of all the Robot instance, access [Bot.instances]
 *
 *
 * @author Him188moe
 * @author NatrualHG
 * @see net.mamoe.mirai.contact.Contact
 */
class Bot(val account: BotAccount, val logger: MiraiLogger) : Closeable {

    val id = createdBotsCount.getAndAdd(1)

    val contacts = ContactSystem()

    val network: BotNetworkHandler = TIMBotNetworkHandler(this)

    init {
        instances.add(this)

        this.logger.identity = "Bot" + this.id + "(" + this.account.qqNumber + ")"
    }

    override fun toString(): String = "Bot{id=$id,qq=${account.qqNumber}}"

    /**
     * Bot 联系人管理.
     *
     * @see Bot.contacts
     */
    inner class ContactSystem internal constructor() {
        val groups = ContactList<Group>()
        val qqs = ContactList<QQ>()

        fun getQQ(qqNumber: Long): QQ {
            synchronized(this.qqs) {
                if (!this.qqs.containsKey(qqNumber)) {
                    this.qqs[qqNumber] = QQ(this@Bot, qqNumber)
                }
                return this.qqs[qqNumber]!!
            }
        }

        fun getGroupByNumber(groupNumber: Long): Group {
            synchronized(this.groups) {
                if (!this.groups.containsKey(groupNumber)) {
                    this.groups[groupNumber] = Group(this@Bot, groupNumber)
                }
                return this.groups[groupNumber]!!
            }
        }

        fun getGroupById(groupId: Long): Group {
            return getGroupByNumber(Group.groupIdToNumber(groupId))
        }
    }

    override fun close() {
        this.network.close()
        this.contacts.groups.values.forEach { it.close() }
        this.contacts.groups.clear()
        this.contacts.qqs.clear()
    }

    companion object {
        val instances: MutableList<Bot> = Collections.synchronizedList(LinkedList())

        private val createdBotsCount = AtomicInteger(0)
    }
}