@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope


/**
 * 群. 在 QQ Android 中叫做 "Troop"
 *
 * Group UIN 与 Group Code 并不是同一个值.
 * Group Code是在客户端显示的code
 * Group Uin是QQ内部的群ID[在Mirai中则为 id]
 * 但是有的时候 两个是相同的value
 * 在网络调用层 Code与Uin会被混用
 * 但在开发层 你应该只关注Group Code
 */
interface Group : Contact, CoroutineScope {

    val groupCode: Long
    /**
     * 群主 (同步事件更新)
     * 进行 [updateGroupInfo] 时将会更新这个值.
     */
    val owner: Member

    /**
     * 群名称 (同步事件更新)
     * 进行 [updateGroupInfo] 时将会更新这个值.
     */
    val name: String

    /**
     * 入群公告, 没有时为空字符串. (同步事件更新)
     * 进行 [updateGroupInfo] 时将会更新这个值.
     */
    val announcement: String

    /**
     * 在 [Group] 实例创建的时候查询一次. 并与事件同步事件更新
     *
     * **注意**: 获得的列表仅为这一时刻的成员列表的镜像. 它将不会被更新
     */
    val members: ContactList<Member>


    /**
     * 获取群成员实例. 若此 ID 的成员不存在, 则会抛出 [kotlin.NoSuchElementException]
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