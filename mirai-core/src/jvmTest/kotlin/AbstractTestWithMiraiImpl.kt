/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal

import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.test.AbstractTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal abstract class AbstractTestWithMiraiImpl : IMirai by MiraiImpl(), AbstractTest() {
    private val originalImpl = Mirai

    @BeforeEach
    fun setupMiraiImpl() {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        net.mamoe.mirai._MiraiInstance.set(this)
    }

    @AfterEach
    fun restoreMiraiImpl() {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        net.mamoe.mirai._MiraiInstance.set(originalImpl)
    }
}