/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.utils

import java.util.concurrent.atomic.AtomicInteger

/**
 * 名称生成器
 *
 * 部分事件没有 `nick`, `name` 等相关的字段以确定名字,
 * [NameGenerator] 的作用就是在无法确定一个准确的名字的时候生成一个默认的名字
 */
public interface NameGenerator {
    public fun nextGroupName(): String
    public fun nextFriendName(): String

    public companion object {
        private val DEFAULT: NameGenerator = SimpleNameGenerator()

        @JvmStatic
        public fun getDefault(): NameGenerator = DEFAULT
    }
}

public open class SimpleNameGenerator : NameGenerator {
    private val groupCounter = AtomicInteger(0)
    private val friendCounter = AtomicInteger(0)

    override fun nextGroupName(): String = "Testing Group #" + groupCounter.getAndIncrement()
    override fun nextFriendName(): String = "Testing Friend #" + friendCounter.getAndIncrement()
}
