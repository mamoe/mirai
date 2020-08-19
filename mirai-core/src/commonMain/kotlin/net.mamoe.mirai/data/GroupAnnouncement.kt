/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * 群公告数据类
 * getGroupAnnouncementList时，如果page=1，那么你可以在inst里拿到一些置顶公告
 *
 * 发公告时只需要填写text，其他参数可为默认值
 *
 */
@MiraiExperimentalAPI
@Serializable
public data class GroupAnnouncementList(
    val ec: Int,  //状态码 0 是正常的
    @SerialName("em") val msg: String,   //信息
    val feeds: List<GroupAnnouncement>? = null,   //群公告列表
    val inst: List<GroupAnnouncement>? = null  //置顶列表？
)

@MiraiExperimentalAPI
@Serializable
public data class GroupAnnouncement(
    @SerialName("u") val sender: Long = 0,
    val msg: GroupAnnouncementMsg,
    val settings: GroupAnnouncementSettings? = null,
    @SerialName("pubt") val time: Long = 0,
    @SerialName("read_num") val readNum: Int = 0,
    @SerialName("is_read") val isRead: Int = 0,
    val pinned: Int = 0,
    val fid: String? = null      //公告的id
)

@MiraiExperimentalAPI
@Serializable
public data class GroupAnnouncementMsg(
    val text: String,
    val text_face: String? = null,
    val title: String? = null
)

@MiraiExperimentalAPI
@Serializable
public data class GroupAnnouncementSettings(
    @SerialName("is_show_edit_card") val isShowEditCard: Int = 0,
    @SerialName("remind_ts") val remindTs: Int = 0,
    @SerialName("tip_window_type") val tipWindowType: Int = 0,
    @SerialName("confirm_required") val confirmRequired: Int = 0
)