@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * 群. 在 QQ Android 中叫做 "Troop"
 */
interface Group : Contact, CoroutineScope {
    /**
     * 群名称.
     *
     * 在修改时将会异步上传至服务器. 无权限修改时将会抛出异常 [PermissionDeniedException]
     *
     * 注: 频繁修改可能会被服务器拒绝.
     */
    var name: String
    /**
     * 入群公告, 没有时为空字符串.
     *
     * 在修改时将会异步上传至服务器. 无权限修改时将会抛出异常 [PermissionDeniedException]
     */
    var announcement: String
    /**
     * 全体禁言状态. `true` 为开启.
     *
     * 当前仅能修改状态.
     */// TODO: 2020/2/5 实现 muteAll 的查询
    var muteAll: Boolean
    /**
     * 坦白说状态. `true` 为允许.
     *
     * 在修改时将会异步上传至服务器. 无权限修改时将会抛出异常 [PermissionDeniedException]
     */
    var confessTalk: Boolean
    /**
     * 允许群员邀请好友入群的状态. `true` 为允许
     *
     * 在修改时将会异步上传至服务器. 无权限修改时将会抛出异常 [PermissionDeniedException]
     */
    var allowMemberInvite: Boolean
    /**
     * 自动加群审批
     */
    val autoApprove: Boolean
    /**
     * 匿名聊天
     */
    val anonymousChat: Boolean

    /**
     * 同为 groupCode, 用户看到的群号码.
     */
    override val id: Long

    /**
     * 群主 (同步事件更新)
     */
    val owner: Member


    /**
     * 机器人在这个群里的权限
     *
     * **MiraiExperimentalAPI**: 在未来可能会被修改
     */
    @MiraiExperimentalAPI
    val botPermission: MemberPermission


    /**
     * 群成员列表, 不含机器人自己, 含群主.
     * 在 [Group] 实例创建的时候查询一次. 并与事件同步事件更新
     */
    val members: ContactList<Member>

    /**
     * 获取群成员实例. 不存在时抛出 [kotlin.NoSuchElementException]
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


    @MiraiExperimentalAPI
    fun toFullString(): String = "Group(id=${this.id}, name=$name, owner=${owner.id}, members=${members.idContentString})"
}