/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal abstract class AbstractEventTest {
    @BeforeEach
    fun loadEventBroadcast() {
        _EventBroadcast.implementation = object : _EventBroadcast() {
            override suspend fun <E : Event> broadcastPublic(event: E): E =
                broadcastImpl(event) // do not call MiraiImpl
        }
    }

    @AfterEach
    fun unloadEventBroadcast() {
        _EventBroadcast.implementation = _EventBroadcast() // restore
    }
}