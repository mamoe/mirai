@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope


/**
 * 群. 在 QQ Android 中叫做 "Troop"
 */
interface Group : Contact, CoroutineScope {

    /**
     * ====以下字段在更新值的时候会自动异步上报服务器更改群信息====
     */
    /**
     * 群名称
     * [可查可改已完成]
     */
    var name: String
    /**
     * 入群公告, 没有时为空字符串
     * [可查可改已完成]
     */
    var announcement: String
    /**
     * 全体禁言状态
     * [可改已完成]
     */
    var muteAll: Boolean
    /**
     * 坦白说状态
     * [可查可改已完成]
     */
    var confessTalk: Boolean
    /**
     * 允许群员拉人状态
     * [可查可改已完成]
     */
    var allowMemberInvite: Boolean
    /**
     * QQ中的自动加群审批
     * [可查已完成]
     */
    val autoApprove: Boolean
    /**
     * 匿名聊天
     * [可查已完成]
     */
    val anonymousChat: Boolean

    /**
     * ====以上字段在更新值的时候会自动异步上报服务器更改群信息====
     */


    /**
     * 同为 groupCode, 用户看到的群号码.
     */
    override val id: Long

    /**
     * 群主 (同步事件更新)
     */
    val owner: Member


    val botPermission: MemberPermission


    /**
     * 在 [Group] 实例创建的时候查询一次. 并与事件同步事件更新
     */
    val members: ContactList<Member>

    /**
     * 获取群成员实例. 若此 id 的成员不存在, 则会抛出 [kotlin.NoSuchElementException]
     */
    operator fun get(id: Long): Member

    /**
     * 获取群成员实例, 不存在则 null
     */
    fun getOrNull(id: Long): Member?

    /**
     * 检查此 id 的群成员是否存在
     */
    operator fun contains(id: Long): Boolean

    /**
     * 让机器人退出这个群. 机器人必须为非群主才能退出. 否则将会失败
     */
    suspend fun quit(): Boolean


    fun toFullString(): String = "Group(id=${this.id}, name=$name, owner=${owner.id}, members=${members.idContentString})"
}