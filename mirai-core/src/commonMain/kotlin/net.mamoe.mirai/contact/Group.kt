@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import net.mamoe.mirai.network.protocol.tim.packet.action.GroupInfo
import net.mamoe.mirai.utils.internal.PositiveNumbers
import net.mamoe.mirai.utils.internal.coerceAtLeastOrFail


/**
 * 群.
 *
 * Group ID 与 Group Number 并不是同一个值.
 * - Group Number([Group.id]) 是通常使用的群号码.(在 QQ 客户端中可见)
 * - Group ID([Group.internalId]) 是与调用 API 时使用的 id.(在 QQ 客户端中不可见)
 * @author Him188moe
 */
interface Group : Contact {
    val internalId: GroupInternalId

    /**
     * 在 [Group] 实例创建的时候查询一次. 收到各事件后
     */
    val member: ContactList<Member>


    suspend fun getMember(id: UInt): Member

    /**
     * 查询群资料
     */ // should be `suspend val` if kotlin supports in the future
    suspend fun queryGroupInfo(): GroupInfo
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
@Suppress("NOTHING_TO_INLINE")
inline fun UInt.groupId(): GroupId = GroupId(this)

/**
 * 将无符号整数格式的 [Long] 转为 [GroupId].
 *
 * 注: 在 Java 中常用 [Long] 来表示 [UInt]
 */
fun @receiver:PositiveNumbers Long.groupId(): GroupId =
    GroupId(this.coerceAtLeastOrFail(0).toUInt())

/**
 * 一些群 API 使用的 ID. 在使用时会特别注明
 *
 * 注: 在引用群 ID 时, 应使用 [GroupId] 或 [GroupInternalId] 类型, 而不是 [UInt]
 *
 * @see GroupInternalId.toId 由 [GroupInternalId] 转换为 [GroupId]
 * @see GroupId.toInternalId 由 [GroupId] 转换为 [GroupInternalId]
 */
inline class GroupInternalId(inline val value: UInt)
