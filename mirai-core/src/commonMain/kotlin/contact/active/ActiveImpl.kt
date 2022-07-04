/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.active

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.contact.active.Active
import net.mamoe.mirai.contact.active.ActiveChart
import net.mamoe.mirai.contact.active.ActiveRecord
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.active.GroupActiveProtocol.getRawGroupActiveData
import net.mamoe.mirai.internal.contact.active.GroupActiveProtocol.getRawGroupLevelInfo
import net.mamoe.mirai.internal.contact.active.GroupActiveProtocol.setGroupLevelInfo
import net.mamoe.mirai.internal.contact.active.GroupActiveProtocol.toActiveChart
import net.mamoe.mirai.internal.contact.active.GroupActiveProtocol.toActiveRecord
import net.mamoe.mirai.internal.contact.groupCode
import net.mamoe.mirai.utils.Either.Companion.onLeft
import net.mamoe.mirai.utils.Either.Companion.onRight
import net.mamoe.mirai.utils.Either.Companion.rightOrNull
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.warning

internal expect class ActiveImpl(
    group: GroupImpl,
    logger: MiraiLogger,
    groupInfo: GroupInfo,
) : CommonActiveImpl

internal abstract class CommonActiveImpl(
    protected val group: GroupImpl,
    protected val logger: MiraiLogger,
    groupInfo: GroupInfo,
) : Active {

    private var _rankTitles: Map<Int, String> = groupInfo.rankTitles

    private var _rankShow: Boolean = groupInfo.rankShow

    private suspend fun getGroupLevelInfo(): GroupLevelInfo? {
        return group.bot.getRawGroupLevelInfo(groupCode = group.groupCode).onLeft {
            if (logger.isEnabled) { // createException
                logger.warning(
                    { "Failed to load rank info for group ${group.id}" },
                    it.createException()
                )
            }
        }.rightOrNull
    }

    private suspend fun rankFlush() {
        val info = getGroupLevelInfo() ?: return
        _rankTitles = info.levelName.mapKeys { (level, _) -> level.removePrefix("lvln").toInt() }
        _rankShow = info.levelFlag == 1

    }

    override var rankTitles: Map<Int, String>
        get() = _rankTitles
        set(newValue) {
            group.checkBotPermission(MemberPermission.ADMINISTRATOR)
            group.launch {
                group.bot.setGroupLevelInfo(groupCode = group.groupCode, titles = newValue).onLeft {
                    if (logger.isEnabled) { // createException
                        logger.warning(
                            { "Failed to set rank titles for group ${group.id}" },
                            it.createException()
                        )
                    }
                }.onRight {
                    rankFlush()
                }
            }
        }

    override var rankShow: Boolean = groupInfo.rankShow
        set(newValue) {
            group.checkBotPermission(MemberPermission.ADMINISTRATOR)
            group.launch {
                group.bot.setGroupLevelInfo(groupCode = group.groupCode, show = newValue).onLeft {
                    if (logger.isEnabled) { // createException
                        logger.warning(
                            { "Failed to set rank show for group ${group.id}" },
                            it.createException()
                        )
                    }
                }.onRight {
                    rankFlush()
                }
            }
        }

    protected suspend fun getGroupActiveData(page: Int?): GroupActiveData? {
        return group.bot.getRawGroupActiveData(group.id, page).onLeft {
            if (logger.isEnabled) { // createException
                logger.warning(
                    { "Failed to load active data for group ${group.id}" },
                    it.createException()
                )
            }
        }.rightOrNull
    }

    override fun asFlow(): Flow<ActiveRecord> {
        return flow {
            var page = 0
            while (currentCoroutineContext().isActive) {
                val result = getGroupActiveData(page = page) ?: break

                result.info.mostAct?.let { emitAll(it.asFlow()) } ?: break

                if (result.info.isEnd == 1) break
                page++
            }
        }.map { it.toActiveRecord(group) }
    }

    override suspend fun getChart(): ActiveChart {
        return getGroupActiveData(page = null)?.info?.toActiveChart() ?: ActiveChart(
            actives = emptyMap(),
            sentences = emptyMap(),
            members = emptyMap(),
            join = emptyMap(),
            exit = emptyMap()
        )
    }
}