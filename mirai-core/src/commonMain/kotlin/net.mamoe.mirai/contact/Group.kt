@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.utils.coerceAtLeastOrFail


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
     * 获取群成员. 若此 ID 的成员不存在, 则会抛出 [kotlin.NoSuchElementException]
     */
    fun getMember(id: Long): Member

    /**
     * 更新群资料. 群资料会与服务器事件同步事件更新, 一般情况下不需要手动更新.
     *
     * @return 这一时刻的群资料
     */
    suspend fun updateGroupInfo(): GroupInfo

    /**
     * 让机器人退出这个群. 机器人必须为非群主才能退出. 否则将会失败
     */
    suspend fun quit(): Boolean

    fun toFullString(): String = "Group(id=${this.id}, name=$name, owner=${owner.id}, members=${members.idContentString})"
}