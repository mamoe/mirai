/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot

/**
 * 拥有 [id] 的对象.
 * 此为 [Contact] 与 [Bot] 的唯一公共接口.
 *
 * @see Contact
 * @see Bot
 */
interface ContactOrBot : CoroutineScope {
    /**
     * QQ 号或群号.
     */
    val id: Long
}