/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.internal.contact.announcement

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import net.mamoe.mirai.contact.announcement.AnnouncementImage
import net.mamoe.mirai.utils.CheckableResponseA
import net.mamoe.mirai.utils.JsonStruct
import net.mamoe.mirai.utils.MiraiInternalApi

@Serializable
internal data class GroupAnnouncementList(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    val feeds: List<GroupAnnouncement>? = null,   //群公告列表
    val inst: List<GroupAnnouncement>? = null  //置顶列表？ 应该是发送给新成员的
) : CheckableResponseA(), JsonStruct {
    /*
  // notes from original implementor, luo123, on 2020/3/13

 * 群公告数据类
 * getGroupAnnouncementList时，如果page=1，那么你可以在inst里拿到一些置顶公告
 *
 * 发公告时只需要填写text，其他参数可为默认值
 *
     */
}

@Serializable
internal data class GroupAnnouncement(
    @SerialName("u") val sender: Long = 0, //发送者id
    val msg: GroupAnnouncementMsg,
    val type: Int = 0, //20 为inst , 6 为feeds
    val settings: GroupAnnouncementSettings = GroupAnnouncementSettings.DEFAULT,
    @SerialName("pubt") val time: Long = 0, //发布时间
    @SerialName("read_num") val readNum: Int = 0, //如果需要确认,则为确认收到的人数,反之则为已经阅读的人数
    @SerialName("is_read") val isRead: Int = 0, //好像没用
    @SerialName("is_all_confirm") val isAllConfirm: Int = 0, //为0 则未全部收到
    val pinned: Int = 0, //1为置顶, 0为默认
    val fid: String? = null,      //公告的id
) : JsonStruct

@Serializable
internal class GroupAnnouncementImage @MiraiInternalApi constructor(
    @SerialName("h") val height: Int,
    @SerialName("w") val width: Int,
    @SerialName("id") val id: String
) : JsonStruct {
    fun toPublic(): AnnouncementImage = AnnouncementImage.create(id, height, width)
}

@Serializable
internal data class GroupAnnouncementMsg(
    val text: String,
    @SerialName("text_face") val textFace: String? = null,
    @SerialName("pics") val images: List<GroupAnnouncementImage> = emptyList(),
//    val title: String? = null // no title any more
)


@Serializable
internal data class GroupAnnouncementSettings(
    @SerialName("is_show_edit_card") val isShowEditCard: Int = 0, //引导群成员修改该昵称  1 引导
    @SerialName("remind_ts") val remindTs: Int = 0,
    @SerialName("tip_window_type") val tipWindowType: Int = 0,  //是否用弹窗展示   1 不使用
    @SerialName("confirm_required") val confirmRequired: Int = 0 // 是否需要确认收到 1 需要
) : JsonStruct {
    companion object {
        val DEFAULT = GroupAnnouncementSettings()
    }
}

@Serializable
internal data class CgiData(
    @SerialName("cgicode") val cgicode: Int,
    @SerialName("data") val `data`: JsonElement,
    @SerialName("msg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct

@Serializable
internal data class GroupAnnouncementReadDetail(
    @SerialName("read_total") val readTotal: Int = 0,
    @SerialName("unread_total") val unreadTotal: Int = 0,
    @SerialName("users") val users: List<User> = emptyList()
) {
    @Serializable
    data class User(
        @SerialName("avatar") val avatar: String,
        @SerialName("display_name") val displayName: String,
        @SerialName("face_flag") val faceFlag: Int,
        @SerialName("uin") val uin: Long
    )
}