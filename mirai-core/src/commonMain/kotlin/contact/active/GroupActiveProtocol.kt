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
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.active.ActiveChart
import net.mamoe.mirai.contact.active.ActiveRecord
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.psKey
import net.mamoe.mirai.internal.network.sKey
import net.mamoe.mirai.utils.*

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
    @SerialName("levelflag") val levelFlag: Int,
    @SerialName("levelname") val levelName: Map<String, String>
) : CheckableResponseA(), JsonStruct

/**
 * 群统计信息
 */
@MiraiExperimentalApi
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

@Suppress("DEPRECATION", "DEPRECATION_ERROR")
internal object GroupActiveProtocol {

    suspend fun QQAndroidBot.getRawGroupLevelInfo(
        groupCode: Long
    ): Either<DeserializationFailure, GroupLevelInfo> {
        return Mirai.Http.get<String> {
            url("https://qinfo.clt.qq.com/cgi-bin/qun_info/get_group_level_info")
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
        }.loadSafelyAs(GroupLevelInfo.serializer())
    }

    suspend fun QQAndroidBot.setGroupLevelInfo(
        groupCode: Long,
        titles: Map<Int, String>
    ): Either<DeserializationFailure, SetResult> {
        return Mirai.Http.post<String> {
            url("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_level_info")
            body = FormDataContent(Parameters.build {
                titles.forEach { (index, name) ->
                    append("lvln$index", name)
                }
                append("gc", groupCode.toString())
                append("src", "qinfo_v3")
                append("bkn", client.wLoginSigInfo.bkn.toString())
            })

            headers {
                // ktor bug
                append(
                    "cookie",
                    "uin=o${id}; skey=${sKey}"
                )
            }
        }.loadSafelyAs(SetResult.serializer())
    }

    suspend fun QQAndroidBot.setGroupLevelInfo(
        groupCode: Long,
        show: Boolean
    ): Either<DeserializationFailure, SetResult> {
        return Mirai.Http.post<String> {
            url("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_setting")
            body = FormDataContent(Parameters.build {
                append("levelflag", if (show) "1" else "0")
                append("gc", groupCode.toString())
                append("src", "qinfo_v3")
                append("bkn", client.wLoginSigInfo.bkn.toString())
            })

            headers {
                // ktor bug
                append(
                    "cookie",
                    "uin=o${id}; skey=${sKey}"
                )
            }
        }.loadSafelyAs(SetResult.serializer())
    }

    suspend fun QQAndroidBot.getRawGroupActiveData(
        groupCode: Long,
        page: Int? = null
    ): Either<DeserializationFailure, GroupActiveData> {
        return Mirai.Http.get<String> {
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
        }.loadSafelyAs(GroupActiveData.serializer())
    }

    fun GroupActiveData.MostActive.toActiveRecord(group: Group): ActiveRecord {
        return ActiveRecordImpl(
            senderId = uin,
            senderName = name,
            sentences = sentencesNum,
            continuation = sta,
            sender = group.get(id = uin)
        )
    }

    fun GroupActiveData.ActiveInfo.toActiveChart(): ActiveChart {
        return ActiveChartImpl(
            actives = actNum?.associate { it.date to it.num }.orEmpty(),
            sentences = sentences?.associate { it.date to it.num }.orEmpty(),
            members = memNum?.associate { it.date to it.num }.orEmpty(),
            join = joinNum?.associate { it.date to it.num }.orEmpty(),
            exit = exitNum?.associate { it.date to it.num }.orEmpty()
        )
    }
}