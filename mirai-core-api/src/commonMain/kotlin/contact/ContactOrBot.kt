/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.NotStableForInheritance

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
     * 头像下载链接, 100px
     * @see getAvatarUrl
     */
    public val avatarUrl: String
        get() = getAvatarUrl(spec = 100)

    /**
     * 头像下载链接,
     * 指定规格不存在时, 会返回 40x40px 的默认图片.
     * @param spec 头像的规格, 单位px
     * 0 (原图), 40 (最高压缩等级), 41 (实际上是40px, 但会比40好一些), 100, 140, 640.
     *
     * 实际上以下格式的url也是可以下载头像的
     * http://q.qlogo.cn/headimg_dl?dst_uin=${id}&spec=${spec}
     */
    public fun getAvatarUrl(spec: Int): String = "http://q.qlogo.cn/g?b=qq&nk=${id}&s=${spec}"
}