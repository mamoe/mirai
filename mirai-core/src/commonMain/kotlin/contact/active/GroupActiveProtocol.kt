/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.active

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.active.*
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.components.HttpClientProvider
import net.mamoe.mirai.internal.network.psKey
import net.mamoe.mirai.internal.network.sKey
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.Either.Companion.mapRight

@Serializable
internal data class SetResult(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?
) : CheckableResponseA(), JsonStruct

/**
 * 群等级信息
 */
@Serializable
internal data class GroupLevelInfo(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?,
    @SerialName("levelflag") val levelFlag: Int = 0,
    @SerialName("levelname") val levelName: Map<String, String> = emptyMap(),
    @SerialName("levelnewflag") val levelNewFlag: Int = 0,
    @SerialName("levelnewname") val levelNewName: Map<String, String> = emptyMap()
) : CheckableResponseA(), JsonStruct

@Serializable
internal data class MemberLevelInfo(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?,
    @SerialName("role") val role: Int = 0,
    @SerialName("mems") val mems: Map<Long, MemberInfo> = emptyMap(),
    @SerialName("lv") val lv: Map<Long, LevelInfo> = emptyMap(),
    @SerialName("levelflag") val levelFlag: Int = 0,
    @SerialName("levelname") val levelName: Map<String, String> = emptyMap(),
    @SerialName("honourflag") val honourFlag: Int = 0
) : CheckableResponseA(), JsonStruct {

    @Serializable
    data class MemberInfo(
        @SerialName("u") val u: Long = 0,
        @SerialName("g") val g: Int = 0,
        @SerialName("n") val n: String = ""
    )

    @Serializable
    data class LevelInfo(
        @SerialName("u") val u: Long = 0,
        @SerialName("d") val d: Int = 0,
        @SerialName("p") val p: Int = 0,
        @SerialName("l") val l: Int = 1
    )
}

/**
 * 群统计信息
 */
@Serializable
internal data class GroupActiveData(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?,
    @SerialName("ginfo") val info: ActiveInfo,
    @SerialName("query") val query: Int? = 0,
    @SerialName("role") val role: Int? = 0
) : CheckableResponseA(), JsonStruct {

    @Serializable
    data class Situation(
        @SerialName("date") val date: String,
        @SerialName("num") val num: Int
    )

    @Serializable
    data class MostActive(
        @SerialName("name") val name: String,  // 名称 不完整
        @SerialName("sentences_num") val sentencesNum: Int,   // 发言数
        @SerialName("sta") val sta: Int = 0,
        @SerialName("uin") val uin: Long = 0
    )

    @Serializable
    data class ActiveInfo(
        @SerialName("g_act_num") val actNum: List<Situation>? = null,    //发言人数列表
        @SerialName("g_createtime") val createTime: Int? = 0,
        @SerialName("g_exit_num") val exitNum: List<Situation>? = null,  //退群人数列表
        @SerialName("g_join_num") val joinNum: List<Situation>? = null,
        @SerialName("g_mem_num") val memNum: List<Situation>? = null,   //人数变化
        @SerialName("g_most_act") val mostAct: List<MostActive>? = null,  //发言排行
        @SerialName("g_sentences") val sentences: List<Situation>? = null,
        @SerialName("gc") val gc: Int? = null,
        @SerialName("gn") val gn: String? = null,
        @SerialName("gowner") val owner: String? = null,
        @SerialName("isEnd") val isEnd: Int
    )
}

@Serializable
internal data class GroupHonorListData(
    @SerialName("gc") val gc: String?,
    @SerialName("type") val type: JsonElement,
    @SerialName("uin") val uin: String?,
    @SerialName("talkativeList") val talkativeList: List<Actor>? = null,
    @SerialName("currentTalkative") val currentTalkative: Current? = null,
    @SerialName("actorList") val actorList: List<Actor>? = null,
    @SerialName("legendList") val legendList: List<Actor>? = null,
    @SerialName("newbieList") val newbieList: List<Actor>? = null,
    @SerialName("strongnewbieList") val strongNewbieList: List<Actor>? = null,
    @SerialName("emotionList") val emotionList: List<Actor>? = null,
    @SerialName("richerList") val richerList: List<Actor>? = null,
    @SerialName("currentRicher") val currentRicher: Current? = null,
    @SerialName("redpacketHonnorList") val redPacketHonorList: List<Actor>? = null,
    @SerialName("currentRedpacketHonnor") val currentRedPacketHonor: Current? = null,
    @SerialName("levelname") val levelName: Map<String, String>? = null,
    @SerialName("manageList") val manageList: List<Tag>? = null, // 管理员
    @SerialName("exclusiveList") val exclusiveList: List<Tag>? = null, // 特殊头衔
    @SerialName("activeObj") val activeObj: Map<String, List<Tag>>? = null, // Key为活跃等级名, 如`冒泡`
    @SerialName("showActiveObj") val showActiveObj: Map<String, Boolean>? = null,
    @SerialName("myTitle") val myTitle: String?,
    @SerialName("myIndex") val myIndex: Int? = 0,
    @SerialName("myAvatar") val myAvatar: String?,
    @SerialName("hasServerError") val hasServerError: Boolean?,
    @SerialName("hwExcellentList") val hwExcellentList: List<Actor>? = null
) : JsonStruct {

    @Serializable
    data class Actor(
        @SerialName("uin") val uin: Long = 0,
        @SerialName("avatar") val avatar: String = "",
        @SerialName("name") val name: String = "",
        @SerialName("desc") val desc: String = "",
        @SerialName("btnText") val btnText: String = "",
        @SerialName("text") val text: String = "",
        @SerialName("icon") val icon: Int? = null
    )

    @Serializable
    data class Current(
        @SerialName("uin") val uin: Long = 0,
        @SerialName("day_count") val dayCount: Int = 0,
        @SerialName("avatar") val avatar: String = "",
        @SerialName("avatar_size") val avatarSize: Int = 0,
        @SerialName("nick") val nick: String = ""
    )

    @Serializable
    data class Tag(
        @SerialName("uin") val uin: Long = 0,
        @SerialName("avatar") val avatar: String = "",
        @SerialName("name") val name: String = "",
        @SerialName("btnText") val btnText: String = "",
        @SerialName("text") val text: String = "",
        @SerialName("tag") val tag: String = "",  // 头衔
        @SerialName("tagColor") val tagColor: String = ""
    )
}

@Serializable
internal data class CgiData(
    @SerialName("cgicode") val cgicode: Int,
    @SerialName("data") val `data`: JsonElement,
    @SerialName("msg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct

@Serializable
internal data class MemberMedalInfo(
    @SerialName("avatar") val avatar: String,
    @SerialName("face_flag") val faceFlag: Int,
    @SerialName("last_view_ts") val lastViewTs: Int,
    @SerialName("list") val list: List<Item>, // 头衔详情
    @SerialName("nick") val nick: String,
    @SerialName("role") val role: Int, // 身份/权限
    @SerialName("weared") val weared: String, // 目前显示头衔
    @SerialName("weared_color") val wearedColor: String // 头衔颜色
) {

    @Serializable
    data class Item(
        @SerialName("achieve_ts") val achieveTs: Int, // 是否拥有
        @SerialName("category_id") val categoryId: Int,
        @SerialName("color") val color: String,
        @SerialName("is_mystery") val isMystery: Int,
        @SerialName("mask") val mask: Int, //  群主 300 管理员 301 特殊 302  活跃 315
        @SerialName("medal_desc") val medalDesc: String,
        @SerialName("name") val name: String,
        @SerialName("order") val order: Int,
        @SerialName("pic") val pic: String,
        @SerialName("rule") val rule: Int,
        @SerialName("rule_desc") val ruleDesc: String, // 来源
        @SerialName("wear_ts") val wearTs: Int // 是否佩戴
    )
}

@Serializable
internal data class MemberHonorInfo(
    @SerialName("add_friend") val addFriend: Int = 0,
    @SerialName("avatar") val avatar: String,
    @SerialName("avatar_size") val avatarSize: Int,
    @SerialName("day_count") val dayCount: Int,
    @SerialName("day_count_history") val dayCountHistory: Int = 1,
    @SerialName("day_count_max") val dayCountMax: Int = 1,
    @SerialName("honor_ids") val honorIds: List<Int> = emptyList(),
    @SerialName("nick") val nick: String,
    @SerialName("uin") val uin: Long,
    @SerialName("update_ymd") val updated: Long = 0, // 格式为 yyyyMMdd 的 数字，表示最后更新时间
)

internal interface MemberHonorList : JsonStruct {
    val current: MemberHonorInfo? get() = null
    val total: Int
    val list: List<MemberHonorInfo>
}

@Serializable
internal data class MemberTalkativeInfo(
    @SerialName("current_talkative") val currentTalkative: MemberHonorInfo? = null,
    @SerialName("talkative_amount") val talkativeAmount: Int,
    @SerialName("talkative_list") val talkativeList: List<MemberHonorInfo>
) : MemberHonorList {
    override val current: MemberHonorInfo? get() = currentTalkative
    override val total: Int get() = talkativeAmount
    override val list: List<MemberHonorInfo> get() = talkativeList
}

@Serializable
internal data class MemberEmotionInfo(
    @SerialName("emotion_list") val emotionList: List<MemberHonorInfo>,
    @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = emotionList
}

@Serializable
internal data class MemberHomeworkExcellentInfo(
    @SerialName("hwexcellent_list") val excellentList: List<MemberHonorInfo>,
    @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = excellentList
}

@Serializable
internal data class MemberHomeworkActiveInfo(
    @SerialName("hwactive_list") val activeList: List<MemberHonorInfo>,
    @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = activeList
}

@Serializable
internal data class MemberContinuousInfo(
    @SerialName("continuous_list") val continuousList: List<MemberHonorInfo>,
    @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = continuousList
}

@Serializable
internal data class MemberRicherHonorInfo(
    @SerialName("current_richer_honor") val currentRicherHonor: MemberHonorInfo? = null,
    @SerialName("richer_amount") val richerAmount: Int,
    @SerialName("richer_honor_list") val richerHonorList: List<MemberHonorInfo>
) : MemberHonorList {
    override val current: MemberHonorInfo? get() = currentRicherHonor
    override val total: Int get() = richerAmount
    override val list: List<MemberHonorInfo> get() = richerHonorList
}

@Serializable
internal data class MemberRedPacketInfo(
    @SerialName("current_redpacket_honor") val currentRedPacketHonor: MemberHonorInfo? = null,
    @SerialName("redpacket_amount") val redPacketAmount: Int,
    @SerialName("redpacket_honor_list") val redPacketHonorList: List<MemberHonorInfo>
) : MemberHonorList {
    override val current: MemberHonorInfo? get() = currentRedPacketHonor
    override val total: Int get() = redPacketAmount
    override val list: List<MemberHonorInfo> get() = redPacketHonorList
}

@Serializable
internal data class MemberScoreData(
    @SerialName("level_list") val levels: List<Level>,
    @SerialName("member_level_list") val mapping: List<MemberLevel>,
    @SerialName("member_title_info") val self: MemberScoreInfo,
    @SerialName("members_list") val members: List<MemberScoreInfo>,
    @SerialName("msg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct {
    @Serializable
    data class Level(
        @SerialName("level") val level: String,
        @SerialName("name") val name: String
    )

    @Serializable
    data class MemberLevel(
        @SerialName("level") val level: Int,
        @SerialName("lower_limit") val lowerLimit: Int,
        @SerialName("mapping_level") val mappingLevel: Int,
        @SerialName("name") val name: String
    )

    @Serializable
    data class MemberScoreInfo(
        @SerialName("level_id") val levelId: Int,
        @SerialName("nf") val nf: Int = 0,
        @SerialName("nick_name") val nickName: String,
        @SerialName("role") val role: Int,
        @SerialName("score") val score: Int,
        @SerialName("uin") val uin: Long
    )
}


internal suspend fun QQAndroidBot.getRawGroupLevelInfo(
    groupCode: Long
): Either<DeserializationFailure, GroupLevelInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qinfo.clt.qq.com/cgi-bin/qun_info/get_group_level_new_info")
        parameter("gc", groupCode)
        parameter("bkn", client.wLoginSigInfo.bkn)
        parameter("src", "qinfo_v3")

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}"
            )
        }
    }.bodyAsText().loadSafelyAs(GroupLevelInfo.serializer())
}

internal suspend fun QQAndroidBot.getRawMemberLevelInfo(
    groupCode: Long
): Either<DeserializationFailure, MemberLevelInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qinfo.clt.qq.com/cgi-bin/qun_info/get_group_members_lite")
        parameter("gc", groupCode)
        parameter("bkn", client.wLoginSigInfo.bkn)
        parameter("src", "qinfo_v3")

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}"
            )
        }
    }.bodyAsText().loadSafelyAs(MemberLevelInfo.serializer())
}

internal suspend fun QQAndroidBot.getRawMemberMedalInfo(
    groupCode: Long,
    uid: Long
): Either<DeserializationFailure, MemberMedalInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/qunwelcome/medal2/list")
        parameter("gc", groupCode)
        parameter("uin", uid)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberMedalInfo.serializer(), it.data)
    }
}

internal suspend fun QQAndroidBot.getRawTalkativeInfo(
    groupCode: Long
): Either<DeserializationFailure, MemberTalkativeInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/qunapp/honor_talkative")
        parameter("gc", groupCode)
        parameter("num", 3000)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberTalkativeInfo.serializer(), it.data)
    }
}

internal suspend fun QQAndroidBot.getRawEmotionInfo(
    groupCode: Long
): Either<DeserializationFailure, MemberEmotionInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/qunapp/honor_emotion")
        parameter("gc", groupCode)
        parameter("num", 3000)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberEmotionInfo.serializer(), it.data)
    }
}

/**
 * @param type 取值 1 2 3 分别对应 学术新星 顶尖学霸 至尊学神
 */
internal suspend fun QQAndroidBot.getRawHomeworkExcellentInfo(
    groupCode: Long,
    type: Int
): Either<DeserializationFailure, MemberHomeworkExcellentInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/qunapp/honor_hwexcellent")
        parameter("gc", groupCode)
        parameter("req_type", type)
        parameter("num", 3000)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberHomeworkExcellentInfo.serializer(), it.data)
    }
}

internal suspend fun QQAndroidBot.getRawHomeworkActiveInfo(
    groupCode: Long
): Either<DeserializationFailure, MemberHomeworkActiveInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/qunapp/honor_hwactive")
        parameter("gc", groupCode)
        parameter("num", 3000)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberHomeworkActiveInfo.serializer(), it.data)
    }
}

/**
 * @param type 取值 2 3 5 分别对应 群聊之火 群聊炽焰 冒尖小春笋
 */
internal suspend fun QQAndroidBot.getRawContinuousInfo(
    groupCode: Long,
    type: Int
): Either<DeserializationFailure, MemberContinuousInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/qunapp/honor_continuous")
        parameter("gc", groupCode)
        parameter("num", 3000)
        parameter("continuous_type", type)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberContinuousInfo.serializer(), it.data)
    }
}

internal suspend fun QQAndroidBot.getRawRicherHonorInfo(
    groupCode: Long
): Either<DeserializationFailure, MemberRicherHonorInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/new_honor/list_honor/list_richer_honor")
        parameter("gc", groupCode)
        parameter("num", 3000)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberRicherHonorInfo.serializer(), it.data)
    }
}

internal suspend fun QQAndroidBot.getRawRedPacketInfo(
    groupCode: Long
): Either<DeserializationFailure, MemberRedPacketInfo> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/new_honor/list_honor/list_redpacket_honor")
        parameter("gc", groupCode)
        parameter("num", 3000)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberRedPacketInfo.serializer(), it.data)
    }
}

/**
 * 只有前 50 名的数据
 */
internal suspend fun QQAndroidBot.getRawMemberTitleList(
    groupCode: Long
): Either<DeserializationFailure, MemberScoreData> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/honorv2/honor_title_list")
        parameter("group_code", groupCode)
        parameter("request_type", "2")
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(CgiData.serializer()).mapRight {
        @Suppress("INVISIBLE_MEMBER")
        defaultJson.decodeFromJsonElement(MemberScoreData.serializer(), it.data)
    }
}

internal suspend fun QQAndroidBot.setGroupLevelInfo(
    groupCode: Long,
    new: Boolean,
    titles: Map<Int, String>
): Either<DeserializationFailure, SetResult> {
    return components[HttpClientProvider].getHttpClient().post {
        url("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_level_info")
        setBody(FormDataContent(Parameters.build {
            titles.forEach { (index, name) ->
                append("lvln$index", name)
            }
            append("new", if (new) "1" else "0")
            append("gc", groupCode.toString())
            append("src", "qinfo_v3")
            append("bkn", client.wLoginSigInfo.bkn.toString())
        }))

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}"
            )
        }
    }.bodyAsText().loadSafelyAs(SetResult.serializer())
}

internal suspend fun QQAndroidBot.setGroupSetting(
    groupCode: Long,
    new: Boolean,
    show: Boolean
): Either<DeserializationFailure, SetResult> {
    return components[HttpClientProvider].getHttpClient().post {
        url("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_setting")
        setBody(FormDataContent(Parameters.build {
            append(if (new) "levelnewflag" else "levelflag", if (show) "1" else "0")
            append("gc", groupCode.toString())
            append("src", "qinfo_v3")
            append("bkn", client.wLoginSigInfo.bkn.toString())
        }))

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}"
            )
        }
    }.bodyAsText().loadSafelyAs(SetResult.serializer())
}

internal suspend fun QQAndroidBot.setGroupHonourFlag(
    groupCode: Long,
    flag: Boolean
): Either<DeserializationFailure, SetResult> {
    return components[HttpClientProvider].getHttpClient().post {
        url("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_honour_flag")
        setBody(FormDataContent(Parameters.build {
            append("gc", groupCode.toString())
            append("bkn", client.wLoginSigInfo.bkn.toString())
            append("src", "qinfo_v3")
            append("flag", if (flag) "0" else "1")
        }))

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}"
            )
        }
    }.bodyAsText().loadSafelyAs(SetResult.serializer())
}

internal suspend fun QQAndroidBot.getRawGroupActiveData(
    groupCode: Long,
    page: Int? = null
): Either<DeserializationFailure, GroupActiveData> {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qqweb.qq.com/c/activedata/get_mygroup_data")
        parameter("bkn", client.wLoginSigInfo.bkn)
        parameter("gc", groupCode)
        parameter("page", page)
        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadSafelyAs(GroupActiveData.serializer())
}

internal suspend fun QQAndroidBot.getRawGroupHonorListData(
    groupId: Long,
    type: GroupHonorType
): Either<DeserializationFailure, GroupHonorListData> {
    val html = components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/interactive/honorlist")
        parameter("gc", groupId)
        parameter(
            "type", when (type) {
                GroupHonorType.BRONZE -> "bronze"
                GroupHonorType.SILVER -> "silver"
                GroupHonorType.GOLDEN -> "golden"
                GroupHonorType.WHIRLWIND -> "whirlwind"
                else -> type.value
            }
        )
        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText()
    val jsonText = html.substringAfter("window.__INITIAL_STATE__=").substringBefore("</script>")
    return jsonText.loadSafelyAs(GroupHonorListData.serializer())
}

@Suppress("INVISIBLE_MEMBER")
internal fun GroupActiveData.MostActive.toActiveRecord(group: Group): ActiveRecord {
    return ActiveRecord(
        memberId = uin,
        memberName = name,
        periodDays = sentencesNum,
        messagesCount = sta,
        member = group.get(id = uin)
    )
}

@Suppress("INVISIBLE_MEMBER")
internal fun GroupActiveData.ActiveInfo.toActiveChart(): ActiveChart {
    return ActiveChart(
        actives = actNum?.associate { it.date to it.num }.orEmpty(),
        sentences = sentences?.associate { it.date to it.num }.orEmpty(),
        members = memNum?.associate { it.date to it.num }.orEmpty(),
        join = joinNum?.associate { it.date to it.num }.orEmpty(),
        exit = exitNum?.associate { it.date to it.num }.orEmpty()
    )
}

@Suppress("INVISIBLE_MEMBER")
internal fun MemberHonorInfo.toActiveHonorInfo(group: GroupImpl): ActiveHonorInfo {
    return ActiveHonorInfo(
        memberName = nick,
        memberId = uin,
        avatar = avatar + avatarSize,
        member = group.get(id = uin),
        termDays = dayCount,
        historyDays = dayCountHistory,
        maxTermDays = dayCountMax
    )
}