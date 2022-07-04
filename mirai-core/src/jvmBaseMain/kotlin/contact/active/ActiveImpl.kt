/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.active

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.active.ActiveRecord
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.active.GroupActiveProtocol.toActiveRecord
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.stream
import java.util.stream.Stream

internal actual class ActiveImpl actual constructor(
    group: GroupImpl,
    logger: MiraiLogger,
    groupInfo: GroupInfo,
) : CommonActiveImpl(group, logger, groupInfo) {

    override fun asStream(): Stream<ActiveRecord> {
        return stream {
            var page = 0
            while (true) {
                val result = runBlocking { getGroupActiveData(page = page) } ?: break

                result.info.mostAct?.let { yieldAll(it) } ?: break

                if (result.info.isEnd == 1) break
                page++
            }
        }.map { it.toActiveRecord(group) }
    }
}