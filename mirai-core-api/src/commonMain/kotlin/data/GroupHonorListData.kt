/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 群荣誉信息
 */
@MiraiExperimentalApi
public enum class GroupHonorType(public val value: Int) {
    TALKATIVE(1),       // 龙王
    PERFORMER(2),       // 群聊之火
    LEGEND(3),          // 群聊炽焰
    STRONG_NEWBIE(5),   // 冒尖小春笋
    EMOTION(6),         // 快乐源泉
    ACTIVE(7),          // 活跃头衔
    EXCLUSIVE(8),       // 特殊头衔
    MANAGE(9);          // 管理头衔

    internal companion object {
        @JvmStatic
        internal fun deserializeFromInt(value: Int): GroupHonorType = values().first { it.value == value }
    }
}

@MiraiExperimentalApi
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
    val activeObj: Map<String, List<Tag?>?>? = null, // Key为活跃等级名, 如`冒泡`

    @SerialName("showActiveObj")
    val showActiveObj: Map<String, Boolean?>? = null,

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

    public object GroupHonorTypeSerializer : KSerializer<GroupHonorType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("GroupHonorTypeSerializer", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: GroupHonorType) {
            encoder.encodeInt(value.value)
        }

        override fun deserialize(decoder: Decoder): GroupHonorType {
            return GroupHonorType.deserializeFromInt(decoder.decodeInt())
        }
    }
}