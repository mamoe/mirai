/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.safeCast

/**
 * 一条在本地构建的, 或其他不保证指向一条服务器上存在的消息的消息源.
 *
 * ## 来源
 *
 * 离线消息源可从[引用回复][QuoteReply]中获得, 因为协议上引用回复中的被引用的消息是由客户端自己提供的.
 * 离线消息源也可通过 [MessageSourceBuilder], [MessageSource.copyAmend] 等方法构建得到.
 *
 * 离线消息源可能来自一条与机器人无关的消息, 因此缺少相关发送环境信息, 无法提供 `sender` 或 `target` 的 [ContactOrBot] 对象的获取.
 *
 * ## 构建
 *
 * - 使用 [MessageSourceBuilder] 可使用相关属性构建实例.
 * - 使用 [MessageSource.copyAmend] 复制另外一个 [MessageSource] 得到.
 * - 使用 [OnlineMessageSource.toOffline] 可将 [OfflineMessageSource] 转换得到 [OfflineMessageSource]. 但这一般没有意义.
 */
@NotStableForInheritance
public abstract class OfflineMessageSource : MessageSource() {
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageSource, OfflineMessageSource>(MessageSource, { it.safeCast() })

    /**
     * 消息种类
     */
    public abstract override val kind: MessageSourceKind

    final override fun toString(): String {
        return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from $fromId to $targetId at $time]"
    }
}