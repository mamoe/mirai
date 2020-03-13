package net.mamoe.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 群公告数据类
 *
 *
 */
@Serializable
data class GroupAnnouncementList(
    val feeds: List<GroupAnnouncement>
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