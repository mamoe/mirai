/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory

/**
 * Without selector. When network is closed, it will not reconnect, so that you can check for its states.
 *
 * @see AbstractCommonNHTestWithSelector
 */
internal actual abstract class AbstractCommonNHTest actual constructor() :
    AbstractRealNetworkHandlerTest<TestCommonNetworkHandler>() {

    actual override val network: TestCommonNetworkHandler
        get() = TODO("Not yet implemented")
    actual override val factory: NetworkHandlerFactory<TestCommonNetworkHandler>
        get() = TODO("Not yet implemented")

    protected actual fun removeOutgoingPacketEncoder() {
    }

    actual val conn: PlatformConn
        get() = TODO("Not yet implemented")

}

internal actual class PlatformConn