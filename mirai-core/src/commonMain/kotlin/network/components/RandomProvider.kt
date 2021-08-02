/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.utils.getValue
import kotlin.random.Random

/**
 * 一些随机数的提供者. 控制随机值, 以可靠地在单元测试中模拟一些过程. 正式环境使用 [ThreadLocalRandomProvider], 一些仿真单元测试中使用固定 [Random] 来提供稳定的可复现数据.
 */
internal interface RandomProvider {
    fun getRandom(requester: Any): Random

    companion object : ComponentKey<RandomProvider> {
        fun ComponentStorage.getRandom(requester: Any): Random = get(RandomProvider).getRandom(requester)
    }
}

internal class ThreadLocalRandomProvider : RandomProvider {
    private val random: Random by ThreadLocal.withInitial { Random(Random.nextInt()) }
    // Implementation should be fast on all platforms. See #936 for relevant issues.

    override fun getRandom(requester: Any): Random = random
}