@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.contact

import com.soywiz.klock.Date
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.singleChain
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.packet.action.RequestProfileDetailsPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.RequestProfileDetailsResponse
import net.mamoe.mirai.network.protocol.tim.packet.action.SendFriendMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.SendGroupMessagePacket
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.sendPacket
import net.mamoe.mirai.utils.SuspendLazy
import net.mamoe.mirai.utils.internal.coerceAtLeastOrFail
import net.mamoe.mirai.withSession

class ContactList<C : Contact> : MutableMap<UInt, C> by mutableMapOf()

/**
 * 联系人. 虽然叫做联系人, 但他的子类有 [QQ] 和 [群][Group].
 *
 * @param bot 这个联系人所属 [Bot]
 * @param id 可以是 QQ 号码或者群号码 [GroupId].
 *
 * @author Him188moe
 */
sealed class Contact(val bot: Bot, val id: UInt) {

    /**
     * 向这个对象发送消息. 速度太快会被服务器拒绝(无响应)
     */
    abstract suspend fun sendMessage(message: MessageChain)


    //这两个方法应写为扩展函数, 但为方便 import 还是写在这里
    suspend fun sendMessage(plain: String) = sendMessage(plain.singleChain())

    suspend fun sendMessage(message: Message) = sendMessage(message.singleChain())
}

/**
 * 一般的用户可见的 ID.
 * 在 TIM/QQ 客户端中所看到的的号码均是这个 ID.
 *
 * 注: 在引用群 ID 时, 应使用 [GroupId] 或 [GroupInternalId] 类型, 而不是 [UInt]
 *
 * @see GroupInternalId.toId 由 [GroupInternalId] 转换为 [GroupId]
 * @see GroupId.toInternalId 由 [GroupId] 转换为 [GroupInternalId]
 */
inline class GroupId(inline val value: UInt)

/**
 * 将 [this] 转为 [GroupId].
 */
fun UInt.groupId(): GroupId = GroupId(this)

/**
 * 将无符号整数格式的 [Long] 转为 [GroupId].
 *
 * 注: 在 Java 中常用 [Long] 来表示 [UInt]
 */
fun Long.groupId(): GroupId = GroupId(this.coerceAtLeastOrFail(0).toUInt())

/**
 * 一些群 API 使用的 ID. 在使用时会特别注明
 *
 * 注: 在引用群 ID 时, 应使用 [GroupId] 或 [GroupInternalId] 类型, 而不是 [UInt]
 *
 * @see GroupInternalId.toId 由 [GroupInternalId] 转换为 [GroupId]
 * @see GroupId.toInternalId 由 [GroupId] 转换为 [GroupInternalId]
 */
inline class GroupInternalId(inline val value: UInt)

/**
 * 群.
 *
 * Group ID 与 Group Number 并不是同一个值.
 * - Group Number([Group.id]) 是通常使用的群号码.(在 QQ 客户端中可见)
 * - Group ID([Group.internalId]) 是与调用 API 时使用的 id.(在 QQ 客户端中不可见)
 * @author Him188moe
 */
@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class Group internal constructor(bot: Bot, val groupId: GroupId) : Contact(bot, groupId.value) {
    val internalId = GroupId(id).toInternalId()
    val members: ContactList<Member>
        get() = TODO("Implementing group members is less important")

    override suspend fun sendMessage(message: MessageChain) {
        bot.sendPacket(SendGroupMessagePacket(bot.qqAccount, internalId, bot.sessionKey, message))
    }

    companion object
}

/**
 * 以 [BotSession] 作为接收器 (receiver) 并调用 [block], 返回 [block] 的返回值.
 * 这个方法将能帮助使用在 [BotSession] 中定义的一些扩展方法, 如 [BotSession.sendAndExpect]
 */
inline fun <R> Contact.withSession(block: BotSession.() -> R): R = bot.withSession(block)

/**
 * QQ 对象.
 * 注意: 一个 [QQ] 实例并不是独立的, 它属于一个 [Bot].
 *
 * A QQ instance helps you to receive event from or sendPacket event to.
 * Notice that, one QQ instance belong to one [Bot], that is, QQ instances from different [Bot] are NOT the same.
 *
 * @author Him188moe
 */
open class QQ internal constructor(bot: Bot, id: UInt) : Contact(bot, id) {
    val profile: Deferred<Profile> by bot.network.SuspendLazy { updateProfile() }

    override suspend fun sendMessage(message: MessageChain) {
        bot.sendPacket(SendFriendMessagePacket(bot.qqAccount, id, bot.sessionKey, message))
    }

    /**
     * 更新个人资料.
     *
     * 这个方法会尽可能更新已有的 [Profile] 对象的值, 而不是用新的对象替换
     * 若 [QQ.profile] 已经初始化, 则在获取到新的 profile 时通过 [Profile.copyFrom] 来更新已有的 [QQ.profile]. 仍然返回 [QQ.profile]
     * 因此, 对于以下代码:
     * ```kotlin
     * val old = qq.profile
     * qq.updateProfile() === old // true, 因为只是更新了 qq.profile 的值
     * ```
     */
    suspend fun updateProfile(): Profile = bot.withSession {
        RequestProfileDetailsPacket(bot.qqAccount, id, sessionKey)
            .sendAndExpect<RequestProfileDetailsResponse, Profile> { it.profile }
            .await().let {
                @Suppress("UNCHECKED_CAST")
                if ((::profile as SuspendLazy<Profile>).isInitialized()) {
                    profile.await().apply { copyFrom(it) }
                } else it
            }
    }

    suspend fun QQ.addAsFriend() {

    }
}


/**
 * 群成员
 */
class Member internal constructor(bot: Bot, id: UInt, val group: Group) : QQ(bot, id) {
    init {
        TODO("Group member implementation")
    }
}

/**
 * 群成员的权限
 */
enum class MemberPermission {
    /**
     * 群主
     */
    OWNER,
    /**
     * 管理员
     */
    OPERATOR,
    /**
     * 一般群成员
     */
    MEMBER;
}

/**
 * 个人资料
 */
class Profile// inline class Date
    (qq: UInt, nickname: String, zipCode: String?, phone: String?, gender: Gender, var birthday: Date?) {

    var qq: UInt = qq
        internal set
    var nickname: String = nickname
        internal set
    var zipCode: String? = zipCode
        internal set
    var phone: String? = phone
        internal set
    var gender: Gender = gender
        internal set
}

fun Profile.copyFrom(another: Profile) {
    this.qq = another.qq
    this.nickname = another.nickname
    this.zipCode = another.zipCode
    this.phone = another.phone
    this.gender = another.gender
}

/**
 * 性别
 */
enum class Gender {
    SECRET,
    MALE,
    FEMALE;
}