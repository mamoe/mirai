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
data class GroupAnnouncementList(
    val ec: Int,  //状态码 0 是正常的
    @SerialName("em") val msg: String,   //信息
    val feeds: List<GroupAnnouncement>? = null,   //群公告列表
    val inst: List<GroupAnnouncement>? = null  //置顶列表？
)

@MiraiExperimentalAPI
@Serializable
data class GroupAnnouncement(
    @SerialName("u") val sender: Long = 0,
    val msg: GroupAnnouncementMsg,
    val settings: GroupAnnouncementSettings? = null,
    @SerialName("pubt") val time: Long = 0,
    @SerialName("read_num") val readNum: Int = 0,
    @SerialName("is_read") val isRead: Int = 0,
    val pinned: Int = 0,
    val fid:String? = null      //公告的id
)

@MiraiExperimentalAPI
@Serializable
data class GroupAnnouncementMsg(
    val text: String,
    val text_face: String? = null,
    val title: String? = null
)

@MiraiExperimentalAPI
@Serializable
data class GroupAnnouncementSettings(
    @SerialName("is_show_edit_card") val isShowEditCard: Int = 0,
    @SerialName("remind_ts") val remindTs: Int = 0,
    @SerialName("tip_window_type") val tipWindowType: Int = 0,
    @SerialName("confirm_required") val confirmRequired: Int = 0
)