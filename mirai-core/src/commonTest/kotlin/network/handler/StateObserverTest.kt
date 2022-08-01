/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import net.mamoe.mirai.internal.network.framework.AbstractMockNetworkHandlerTest
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.CONNECTING
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.INITIALIZED
import net.mamoe.mirai.internal.network.handler.state.CombinedStateObserver
import net.mamoe.mirai.internal.network.handler.state.CombinedStateObserver.Companion.plus
import net.mamoe.mirai.internal.network.handler.state.StateChangedObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class StateObserverTest : AbstractMockNetworkHandlerTest() {
    @Test
    fun `can trigger observer`() {
        val called = ArrayList<Pair<NetworkHandlerSupport.BaseStateImpl, NetworkHandlerSupport.BaseStateImpl>>()
        components[StateObserver] = object : StateObserver {
            override fun stateChanged(
                networkHandler: NetworkHandlerSupport,
                previous: NetworkHandlerSupport.BaseStateImpl,
                new: NetworkHandlerSupport.BaseStateImpl
            ) {
                called.add(previous to new)
            }
        }
        val handler = createNetworkHandler()
        assertEquals(0, called.size)
        handler.setState(INITIALIZED)
        assertEquals(1, called.size)
        assertEquals(INITIALIZED, called[0].first.correspondingState)
        assertEquals(INITIALIZED, called[0].second.correspondingState)
        handler.setState(CONNECTING)
        assertEquals(2, called.size)
        assertEquals(INITIALIZED, called[1].first.correspondingState)
        assertEquals(CONNECTING, called[1].second.correspondingState)
    }

    @Test
    fun `test StateChangedObserver`() {
        val called = ArrayList<Pair<NetworkHandlerSupport.BaseStateImpl, NetworkHandlerSupport.BaseStateImpl>>()
        components[StateObserver] = object : StateChangedObserver(CONNECTING) {
            override fun stateChanged0(
                networkHandler: NetworkHandlerSupport,
                previous: NetworkHandlerSupport.BaseStateImpl,
                new: NetworkHandlerSupport.BaseStateImpl
            ) {
                called.add(previous to new)
            }
        }
        val handler = createNetworkHandler()
        assertEquals(0, called.size)
        handler.setState(INITIALIZED)
        assertEquals(0, called.size)
        handler.setState(CONNECTING)
        assertEquals(1, called.size)
        assertEquals(INITIALIZED, called[0].first.correspondingState)
        assertEquals(CONNECTING, called[0].second.correspondingState)
    }

    @Test
    fun `test StateChangedObserver2`() {
        val called = ArrayList<NetworkHandlerSupport.BaseStateImpl>()
        components[StateObserver] = StateChangedObserver("test", INITIALIZED, CONNECTING) { new ->
            called.add(new)
        }
        val handler = createNetworkHandler()
        assertEquals(0, called.size)
        handler.setState(INITIALIZED)
        assertEquals(0, called.size)
        handler.setState(CONNECTING)
        assertEquals(1, called.size)
        assertEquals(CONNECTING, called[0].correspondingState)
    }

    @Test
    fun `can combine`() {
        val called = ArrayList<Pair<NetworkHandlerSupport.BaseStateImpl, NetworkHandlerSupport.BaseStateImpl>>()
        val added = object : StateChangedObserver(CONNECTING) {
            override fun stateChanged0(
                networkHandler: NetworkHandlerSupport,
                previous: NetworkHandlerSupport.BaseStateImpl,
                new: NetworkHandlerSupport.BaseStateImpl
            ) {
                called.add(previous to new)
            }

            override fun toString(): String = "1"
        } + object : StateChangedObserver(CONNECTING) {
            override fun stateChanged0(
                networkHandler: NetworkHandlerSupport,
                previous: NetworkHandlerSupport.BaseStateImpl,
                new: NetworkHandlerSupport.BaseStateImpl
            ) {
                called.add(previous to new)
            }

            override fun toString(): String = "2"
        }
        assertIs<CombinedStateObserver>(added)
        assertEquals("CombinedStateObserver[1 -> 2]", added.toString())

        components[StateObserver] = added
        val handler = createNetworkHandler()
        assertEquals(0, called.size)
        handler.setState(INITIALIZED)
        assertEquals(0, called.size)
        handler.setState(CONNECTING)
        assertEquals(2, called.size)
        assertEquals(INITIALIZED, called[0].first.correspondingState)
        assertEquals(CONNECTING, called[0].second.correspondingState)
        assertEquals(INITIALIZED, called[1].first.correspondingState)
        assertEquals(CONNECTING, called[1].second.correspondingState)
    }
}