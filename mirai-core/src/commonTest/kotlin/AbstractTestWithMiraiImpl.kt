/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.internal

import net.mamoe.mirai.Mirai
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal abstract class AbstractTestWithMiraiImpl : MiraiImpl() {
    private val originalImpl = Mirai

    @BeforeTest
    fun setupMiraiImpl() {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        net.mamoe.mirai._MiraiInstance.set(this)
    }

    @AfterTest
    fun restoreMiraiImpl() {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        net.mamoe.mirai._MiraiInstance.set(originalImpl)
    }
}