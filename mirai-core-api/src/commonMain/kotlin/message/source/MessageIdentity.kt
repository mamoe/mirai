/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.source

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.ids
import net.mamoe.mirai.utils.NotStableForInheritance
import org.jetbrains.annotations.ApiStatus

/**
 * 用于定位消息的消息凭证符
 *
 * @since 2.16.0
 */
@NotStableForInheritance
@ApiStatus.NonExtendable
@Serializable(MessageIdentitySerializer::class)
public interface MessageIdentity {

    /**
     * 消息 ids (序列号). 在获取失败时 (概率很低) 为空数组.
     *
     * ### 顺序
     * 群消息的 id 由服务器维护. 好友消息的 id 由 mirai 维护.
     * 此 id 不一定从 0 开始.
     *
     * - 在同一个群的消息中此值随每条消息递增 1, 但此行为由服务器决定, mirai 不保证自增顺序.
     * - 在好友消息中无法保证每次都递增 1. 也可能会产生大幅跳过的情况.
     *
     * ### 多 ID 情况
     * 对于单条消息, [ids] 为单元素数组. 对于分片 (一种长消息处理机制) 消息, [ids] 将包含多元素.
     *
     * [internalIds] 与 [ids] 以数组下标对应.
     */
    public val ids: IntArray

    /**
     * 内部 ids. **仅用于协议模块使用**
     *
     * 值没有顺序, 也可能为 0, 取决于服务器是否提供.
     *
     * 在事件中和在引用中无法保证同一条消息的 [internalIds] 相同.
     *
     * [internalIds] 与 [ids] 以数组下标对应.
     *
     * @see ids
     */
    public val internalIds: IntArray

    /**
     * 发送时间时间戳, 单位为秒.
     */
    public val time: Int


    public fun convertToRawMessageIdentity(): RawMessageIdentity {
        return RawMessageIdentity(
            ids = this.ids,
            internalIds = this.internalIds,
            time = this.time,
        )
    }
}

/**
 * 用于定位消息的消息凭证符, 附带发送者 id 和消息类型
 *
 * @since 2.16.0
 */
@NotStableForInheritance
@ApiStatus.NonExtendable
@Serializable(FullyMessageIdentitySerializer::class)
public interface FullyMessageIdentity : MessageIdentity {

    /**
     * 发送人用户 ID.
     */
    public val fromId: Long // sender

    /**
     * 消息发送目标用户或群号码.
     */
    public val targetId: Long // groupCode / friendUin / memberUin

    /**
     * 消息种类
     */
    public val kind: MessageSourceKind

    public fun convertToRawFullyMessageIdentity(): RawFullyMessageIdentity {
        return RawFullyMessageIdentity(
            ids = ids,
            internalIds = internalIds,
            time = time,
            fromId = fromId,
            targetId = targetId,
            kind = kind,
        )
    }
}
