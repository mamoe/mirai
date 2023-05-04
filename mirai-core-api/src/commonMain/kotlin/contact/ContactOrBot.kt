/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.NotStableForInheritance
import kotlin.jvm.JvmName

/**
 * 拥有 [id] 的对象.
 * 此为 [Contact] 与 [Bot] 的唯一公共接口.
 *
 * @see UserOrBot
 *
 * @see Contact
 * @see Bot
 */
@NotStableForInheritance
public interface ContactOrBot : CoroutineScope {
    /**
     * QQ 号或群号.
     */
    public val id: Long

    /**
     * 相关 [Bot]
     */
    public val bot: Bot

    /**
     * 头像下载链接, 规格默认为 [AvatarSpec.LARGEST]
     * @see avatarUrl
     */
    public val avatarUrl: String
        get() = avatarUrl(spec = AvatarSpec.LARGEST)

    /**
     * 头像下载链接.
     * @param spec 头像的规格.
     * @since 2.11
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("getAvatarUrl")
    public fun avatarUrl(spec: AvatarSpec): String {
        @OptIn(MiraiInternalApi::class)
        return "http://q.qlogo.cn/g?b=qq&nk=${id}&s=${spec.size}"
    }
}