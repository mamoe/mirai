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
import java.time.Instant

@MiraiExperimentalApi
public class Announcement(
    /**
     *  公告的fid，每个公告仅有一条fid，类似于主键
     */
    public val fid: String,

    /**
     *  公告发送者的QQ号
     */
    public val senderId: Long,

    /**
     *  公告的标题
     */
    public val title: String,

    /**
     *  公告的内容
     */
    public val msg: String,

    /**
     *  是否发送给新成员
     */
    public val sendToNewMember: Boolean = false,

    /**
     *  公告发出的时间，为EpochSecond(自 1970-01-01T00：00：00Z 的秒数)
     *  只读属性
     *
     * @see Instant.ofEpochSecond
     */
    public val publishTime: Long = 0,

    /**
     *  是否置顶，可以有多个置顶公告
     */
    public val isPinned: Boolean = false,

    /**
     *  是否显示能够引导群成员修改昵称窗口
     */
    public val isShowEditCard: Boolean = false,

    /**
     *  是否使用弹窗
     */
    public val isTip: Boolean = false,

    /**
     *  是否需要群成员确认
     */
    public val needConfirm: Boolean = false,

    /**
     *  所有人都已阅读, 如果[Announcement.needConfirm]为true则为所有人都已确认,
     *  只读属性
     */
    public val isAllRead: Boolean = false,

    /**
     *  已经阅读的成员数量，如果[Announcement.needConfirm]为true则为已经确认的成员数量
     *  只读属性
     */
    public val readMemberNumber: Int = 0
)