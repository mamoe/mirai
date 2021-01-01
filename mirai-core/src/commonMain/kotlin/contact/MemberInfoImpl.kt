/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopMemberInfo

internal class MemberInfoImpl(
    override val uin: Long,
    override var nick: String,
    override val permission: MemberPermission,
    override var remark: String,
    override val nameCard: String,
    override val specialTitle: String,
    override val muteTimestamp: Int,
    override val anonymousId: String?,
) : MemberInfo, UserInfoImpl(uin, nick, remark) {
    constructor(
        jceInfo: StTroopMemberInfo,
        groupOwnerId: Long
    ) : this(
        uin = jceInfo.memberUin,
        nick = jceInfo.nick,
        permission = when {
            jceInfo.memberUin == groupOwnerId -> MemberPermission.OWNER
            jceInfo.dwFlag == 1L -> MemberPermission.ADMINISTRATOR
            else -> MemberPermission.MEMBER
        },
        remark = jceInfo.autoRemark.orEmpty(),
        nameCard = jceInfo.sName.orEmpty(),
        specialTitle = jceInfo.sSpecialTitle.orEmpty(),
        muteTimestamp = jceInfo.dwShutupTimestap?.toInt() ?: 0,
        anonymousId = null,
    )
}