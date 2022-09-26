/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.utils.NotStableForInheritance

@NotStableForInheritance
public interface Channel : Contact, CoroutineScope, FileSupported, AudioSupported {

    /**
     * 子频道名称
     */
    public val name: String

    /**
     * 子频道ID
     */
    public override val id: Long

    /**
     * 所属频道Id
     */
    public val guildId: Long

}