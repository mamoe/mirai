/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.internal.QQAndroidBot
import kotlin.coroutines.CoroutineContext

internal actual class GroupImpl actual constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    id: Long,
    groupInfo: GroupInfo,
    members: ContactList<NormalMemberImpl>,
) : Group, CommonGroupImpl(bot, parentCoroutineContext, id, groupInfo, members) {
    actual companion object;
}