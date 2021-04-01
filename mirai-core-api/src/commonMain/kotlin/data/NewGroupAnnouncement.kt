/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.utils.MiraiExperimentalApi

@MiraiExperimentalApi
public data class Announcement(
    public val fid: String,
    public val senderId: Long,
    public val publishTime: Long,
    public val title: String,
    public val msg: String,
    // 是否置顶
    public val isTop: Boolean = false,
    // 是否发送给新成员
    public val sendToNewMember: Boolean = false,
    // 已经阅读的成员数量, 如果是confirm则为已经确认的成员数量
    public val readMemberNumber: Int = 0,
    // 是否使用弹窗
    public val needUseTip: Boolean = false,
    // 是否需要确认
    public val needConfirm: Boolean = false,
    // 是否能够引导群成员修改昵称
    public val isShowEditCard: Boolean = false,
    // 所有人都已接收
//    public val isAllRead:Boolean,
)