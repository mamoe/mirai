/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.essence

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiLogger
import java.util.stream.Stream

internal actual class EssencesImpl actual constructor(
    group: GroupImpl,
    logger: MiraiLogger,
) : CommonEssencesImpl(group, logger) {

    override fun asStream(): Stream<MessageSource> {
        TODO("Not yet implemented")
    }
}