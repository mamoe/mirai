/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.active

import net.mamoe.mirai.contact.active.ActiveChart

internal data class ActiveChartImpl(
    override val actives: Map<String, Int>,
    override val sentences: Map<String, Int>,
    override val members: Map<String, Int>,
    override val join: Map<String, Int>,
    override val exit: Map<String, Int>
) : ActiveChart