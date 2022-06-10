/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.internal.message.image.InternalImageProtocolImpl
import net.mamoe.mirai.internal.notice.processors.AbstractNoticeProcessorTest
import kotlin.test.Test
import kotlin.test.assertIs

internal class InternalImageProtocolImplTest : AbstractNoticeProcessorTest() { // borrow Bot testkit
    val instance = InternalImageProtocolImpl()

    @Test
    fun testFindChecker() {
        assertIs<InternalImageProtocolImpl.ImageUploadedCheckerGroup>(instance.findChecker(setBot(1).addGroup(2, 3)))
        assertIs<InternalImageProtocolImpl.ImageUploadedCheckerUser>(instance.findChecker(setBot(1).addFriend(2)))
        assertIs<InternalImageProtocolImpl.ImageUploadedCheckerFallback>(instance.findChecker(null))

        // these 3 tests are complete -- no need to add more when adding more checkers.
    }
}