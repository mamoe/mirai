/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import net.mamoe.mirai.internal.test.AbstractTest

internal abstract class AbstractEventTest : AbstractTest() {
    init {
//        System.setProperty("mirai.event.trace", "true") // Do not set it to true by default, or the concurrency stress test will become extremely slow.
    }
}