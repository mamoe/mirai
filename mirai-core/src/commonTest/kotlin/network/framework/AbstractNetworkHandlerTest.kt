/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.internal.network.handler.selector.AbstractKeepAliveNetworkHandlerSelector
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.utils.setSystemProp
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * @see AbstractCommonNHTest
 * @see AbstractCommonNHTestWithSelector
 * @see AbstractMockNetworkHandlerTest
 */
internal sealed class AbstractNetworkHandlerTest : AbstractTest() {
    ///////////////////////////////////////////////////////////////////////////
    // Defaults
    ///////////////////////////////////////////////////////////////////////////

    init {
        setSystemProp("mirai.event.launch.undispatched", "true") // allow us to do some
    }

    @BeforeTest
    fun be() {
        AbstractKeepAliveNetworkHandlerSelector.RECONNECT_DELAY = 0
    }

    @AfterTest
    fun af() {
        AbstractKeepAliveNetworkHandlerSelector.RECONNECT_DELAY = 3000
    }
}