package net.mamoe.mirai.data

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * 群荣誉信息
 */

public enum class GroupHonorType(public val value: Int) {
    // ACTIVE(7)        // 活跃头衔
    TALKATIVE(1),       // 龙王
    PERFORMER(2),       // 群聊之火
    LEGEND(3),          // 群聊炽焰
    STRONG_NEWBIE(5),   // 冒尖小春笋
    EMOTION(6),         // 快乐源泉
    EXCLUSIVE(8),       // 特殊头衔
    MANAGE(9);          // 管理头衔

    public companion object {
        public fun fromInt(value: Int): GroupHonorType = values().first { it.value == value }
    }
}

@MiraiExperimentalAPI
@Serializable
public data class GroupHonorListData(
    @SerialName("acceptLanguages")
    val acceptLanguages: List<Language?>? = null,

    @SerialName("gc")
    val gc: String?,

    @Serializable(with = GroupHonorTypeSerializer::class)
    @SerialName("type")
    val type: GroupHonorType?,

    @SerialName("uin")
    val uin: String?,

    @SerialName("talkativeList")
    val talkativeList: List<Talkative?>? = null,

    @SerialName("currentTalkative")
    val currentTalkative: CurrentTalkative? = null,

    @SerialName("actorList")
    val actorList: List<Actor?>? = null,

    @SerialName("legendList")
    val legendList: List<Actor?>? = null,

    @SerialName("newbieList")
    val newbieList: List<Actor?>? = null,

    @SerialName("strongnewbieList")
    val strongNewbieList: List<Actor?>? = null,

    @SerialName("emotionList")
    val emotionList: List<Actor?>? = null,

    @SerialName("levelname")
    val levelName: LevelName? = null,

    @SerialName("manageList")
    val manageList: List<Tag?>? = null,

    @SerialName("exclusiveList")
    val exclusiveList: List<Tag?>? = null,

    @SerialName("activeObj")
    val activeObj: PlaceHolder? = null,

    @SerialName("showActiveObj")
    val showActiveObj: PlaceHolder? = null,

    @SerialName("myTitle")
    val myTitle: String?,

    @SerialName("myIndex")
    val myIndex: Int? = 0,

    @SerialName("myAvatar")
    val myAvatar: String?,

    @SerialName("hasServerError")
    val hasServerError: Boolean?,

    @SerialName("hwExcellentList")
    val hwExcellentList: List<Actor?>? = null
) {
    /**
     * 对于活跃头衔, 对象Key为等级名称, 可自定义, 故不固定, 此处未对其进行支持, 先占位
     */
    @Serializable
    public data class PlaceHolder(
        @SerialName("placeHolder")
        val placeHolder: String? = null
    )

    @Serializable
    public data class Language(
        @SerialName("code")
        val code: String? = null,

        @SerialName("script")
        val script: String? = null,

        @SerialName("region")
        val region: String? = null,

        @SerialName("quality")
        val quality: Double? = null
    )

    @Serializable
    public data class Actor(
        @SerialName("uin")
        val uin: Long? = 0,

        @SerialName("avatar")
        val avatar: String? = null,

        @SerialName("name")
        val name: String? = null,

        @SerialName("desc")
        val desc: String? = null,

        @SerialName("btnText")
        val btnText: String? = null,

        @SerialName("text")
        val text: String? = null,

        @SerialName("icon")
        val icon: Int?
    )

    @Serializable
    public data class Talkative(
        @SerialName("uin")
        val uin: Long? = 0,

        @SerialName("avatar")
        val avatar: String? = null,

        @SerialName("name")
        val name: String? = null,

        @SerialName("desc")
        val desc: String? = null,

        @SerialName("btnText")
        val btnText: String? = null,

        @SerialName("text")
        val text: String? = null
    )

    @Serializable
    public data class CurrentTalkative(
        @SerialName("uin")
        val uin: Long? = 0,

        @SerialName("day_count")
        val dayCount: Int? = null,

        @SerialName("avatar")
        val avatar: String? = null,

        @SerialName("avatar_size")
        val avatarSize: Int? = null,

        @SerialName("nick")
        val nick: String? = null
    )

    @Serializable
    public data class LevelName(
        @SerialName("lvln1")
        val lv1: String? = null,

        @SerialName("lvln2")
        val lv2: String? = null,

        @SerialName("lvln3")
        val lv3: String? = null,

        @SerialName("lvln4")
        val lv4: String? = null,

        @SerialName("lvln5")
        val lv5: String? = null,

        @SerialName("lvln6")
        val lv6: String? = null
    )

    @Serializable
    public data class Tag(
        @SerialName("uin")
        val uin: Long? = 0,

        @SerialName("avatar")
        val avatar: String? = null,

        @SerialName("name")
        val name: String? = null,

        @SerialName("btnText")
        val btnText: String? = null,

        @SerialName("text")
        val text: String? = null,

        @SerialName("tag")
        val tag: String? = null,  // 头衔

        @SerialName("tagColor")
        val tagColor: String? = null
    )

    @Serializer(forClass = GroupHonorType::class)
    public object GroupHonorTypeSerializer : KSerializer<GroupHonorType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("GroupHonorTypeSerializer", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: GroupHonorType) {
            encoder.encodeInt(value.value)
        }

        override fun deserialize(decoder: Decoder): GroupHonorType {
            return GroupHonorType.fromInt(decoder.decodeInt())
        }
    }
}