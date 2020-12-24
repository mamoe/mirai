/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.MiraiInternalApi
import java.util.concurrent.ConcurrentLinkedQueue

public class OtherClientList internal constructor(
    @MiraiInternalApi @JvmField
    public val delegate: MutableCollection<OtherClient> = ConcurrentLinkedQueue()
) : Collection<OtherClient> by delegate {
    public operator fun get(kind: ClientKind): OtherClient? = this.find { it.kind == kind }

    public fun getOrFail(kind: ClientKind): OtherClient =
        get(kind) ?: throw NoSuchElementException("OtherClient with kind=$kind not found.")
}