/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils


public interface TimeSource {
    public fun currentTimeMillis(): Long
    public fun currentTimeSeconds(): Long = currentTimeMillis() / 1000

    public object System : TimeSource {
        override fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()
    }
}
