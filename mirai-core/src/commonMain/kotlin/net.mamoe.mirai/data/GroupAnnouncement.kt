package net.mamoe.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 群公告数据类
 * getGroupAnnouncementList时，如果page=1，那么你可以在inst里拿到一些置顶公告
 *
 *
 */
@Serializable
data class GroupAnnouncementList(
    val ec: Int,  //状态码 0 是正常的
    @SerialName("em") val msg:String,   //信息
    val feeds: List<GroupAnnouncement>?,   //群公告列表
    val inst: List<GroupAnnouncement>?  //置顶列表？
)

@Serializable
data class GroupAnnouncement(
    @SerialName("u") val sender: Long,
    val msg: GroupAnnouncementMsg,
    val settings: GroupAnnouncementSettings
)

@Serializable
data class GroupAnnouncementMsg(
    val text: String,
    val text_face: String,
    val title: String
)

@Serializable
data class GroupAnnouncementSettings(
    @SerialName("is_show_edit_card") val isShowEditCard: Int,
    @SerialName("remind_ts") val remindTs: Int,
    @SerialName("tip_window_type") val tipWindowType: Int,
    @SerialName("confirm_required") val confirmRequired: Int
)