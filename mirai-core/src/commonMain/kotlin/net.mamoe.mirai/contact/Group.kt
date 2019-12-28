@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.utils.coerceAtLeastOrFail


/**
 * 群.
 *
 * Group ID 与 Group Number 并不是同一个值.
 * - Group Number([Group.id]) 是通常使用的群号码.(在 QQ 客户端中可见)
 * - Group ID([Group.internalId]) 是与调用 API 时使用的 id.(在 QQ 客户端中不可见)
 * @author Him188moe
 */
interface Group : Contact, CoroutineScope/*, Map<UInt, Member>*/ { // TODO: 2019/12/4 在 inline 稳定后实现 Map<UInt, Member>. 目前这样做会导致问题
    /**
     * 内部 ID. 内部 ID 为 [GroupId] 的映射
     */
    val internalId: GroupInternalId

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
     *
     * @see QuitGroupResponse.isSuccess 判断是否成功
     */
    suspend fun quit(): Boolean

    fun toFullString(): String = "Group(id=${this.id}, name=$name, owner=${owner.id}, members=${members.idContentString})"
}

/**
 * 一般的用户可见的 ID.
 * 在 TIM/QQ 客户端中所看到的的号码均是这个 ID.
 *
 * 注: 在引用群 ID 时, 只应使用 [GroupId] 或 [GroupInternalId] 类型 (内联类无性能损失), 而不能使用 [UInt].
 *
 * @see GroupInternalId.toId 由 [GroupInternalId] 转换为 [GroupId]
 * @see GroupId.toInternalId 由 [GroupId] 转换为 [GroupInternalId]
 */
inline class GroupId(inline val value: Long)

/**
 * 将 [this] 转为 [GroupInternalId].
 */
fun Long.groupInternalId(): GroupInternalId = GroupInternalId(this)

/**
 * 将无符号整数格式的 [Long] 转为 [GroupId].
 *
 * 注: 在 Java 中常用 [Long] 来表示 [UInt]
 */
fun Long.groupId(): GroupId = GroupId(this.coerceAtLeastOrFail(0))

/**
 * 一些群 API 使用的 ID. 在使用时会特别注明
 *
 * 注: 在引用群 ID 时, 应使用 [GroupId] 或 [GroupInternalId] 类型, 而不是 [UInt]
 *
 * @see GroupInternalId.toId 由 [GroupInternalId] 转换为 [GroupId]
 * @see GroupId.toInternalId 由 [GroupId] 转换为 [GroupInternalId]
 */
inline class GroupInternalId(inline val value: Long)
