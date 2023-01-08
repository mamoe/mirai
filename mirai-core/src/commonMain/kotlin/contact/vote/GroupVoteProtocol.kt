/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.vote

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.HttpClientProvider
import net.mamoe.mirai.internal.network.psKey
import net.mamoe.mirai.internal.network.sKey
import net.mamoe.mirai.utils.CheckableResponseA
import net.mamoe.mirai.utils.JsonStruct
import net.mamoe.mirai.utils.loadAs

@Serializable
internal data class GroupVoteList(
    @SerialName("ad") val ad: Int = 0,
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("feeds") val votes: List<GroupVoteInfo> = emptyList(),
    @SerialName("gln") val gln: Int = 0,
    @SerialName("ltsm") val ltsm: Int = 0,
    @SerialName("read_only") val readOnly: Int = 0,
    @SerialName("role") val role: Int = 0,
    @SerialName("server_time") val serverTime: Long = 0,
    @SerialName("srv_code") val srvCode: Int = 0,
    @SerialName("sta") val sta: Int = 0,
    @SerialName("svrt") val svrt: Int = 0,
    @SerialName("tst") val tst: Int = 0,
    @SerialName("ui") val ui: Map<Int, UserInfo> = emptyMap()
) : CheckableResponseA(), JsonStruct

@Serializable
internal data class GroupVote(
    @SerialName("cn") val cn: Int = 0,
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("fid") val fid: String = "",
    @SerialName("fn") val fn: Int = 0,
    @SerialName("gmn") val gmn: Int = 0,
    @SerialName("group") val group: Group = Group(),
    @SerialName("jointime") val jointime: Int = 0,
    @SerialName("ltsm") val ltsm: Int = 0,
    @SerialName("msg") val detail: GroupVoteDetail = GroupVoteDetail(),
    @SerialName("pubt") val pubt: Int = 0,
    @SerialName("read_only") val readOnly: Int = 0,
    @SerialName("role") val role: Int = 0,
    @SerialName("server_time") val serverTime: Long = 0,
    @SerialName("srv_code") val srvCode: Int = 0,
    @SerialName("svrt") val svrt: Int = 0,
    @SerialName("type") val type: Int = 0,
    @SerialName("u") val u: Long = 0,
    @SerialName("ui") val ui: Map<Int, UserInfo> = emptyMap(),
    @SerialName("vn") val vn: Int = 0
) : CheckableResponseA(), JsonStruct {
    @Serializable
    data class Group(
        @SerialName("class_ext") val classExt: Int = 0,
        @SerialName("group_id") val groupId: Int = 0
    )

}

@Serializable
internal data class UserInfo(
    @SerialName("f") val face: String = "",
    @SerialName("n") val nick: String = ""
)

@Serializable
internal data class GroupVoteContent(
    @SerialName("text") val text: String = ""
)

@Serializable
internal data class GroupVoteDetail(
    @SerialName("dl") val dl: Int = 0,
    @SerialName("mo") val mo: Int = 0,
    @SerialName("op") val options: List<Option> = emptyList(),
    @SerialName("sta") val sta: Int = 0,
    @SerialName("t") val title: GroupVoteContent = GroupVoteContent(),
    @SerialName("us") val us: List<Int> = emptyList(),
    @SerialName("vr") val result: List<VoteResult> = emptyList(),
    @SerialName("vsb") val vsb: Int = 0
) {
    @Serializable
    data class Option(
        @SerialName("c") val content: GroupVoteContent = GroupVoteContent(),
        @SerialName("sn") val sn: Int = 0
    )

    @Serializable
    data class VoteResult(
        @SerialName("o") val option: List<Int> = emptyList(),
        @SerialName("t") val time: Long = 0,
        @SerialName("u") val uid: Long = 0
    )
}

@Serializable
internal data class GroupVoteSettings(
    @SerialName("confirm_required") val confirmRequired: Int = 0,
    @SerialName("is_show_edit_card") val isShowEditCard: Int = 0,
    @SerialName("remind_ts") val remindTs: Int = 0,
    @SerialName("tip_window_type") val tipWindowType: Int = 0
)

@Serializable
internal data class GroupVoteInfo(
    @SerialName("cn") val cn: Int = 0,
    @SerialName("fid") val fid: String = "",
    @SerialName("fn") val fn: Int = 0,
    @SerialName("msg") val detail: GroupVoteDetail = GroupVoteDetail(),
    @SerialName("pubt") val published: Int = 0,
    @SerialName("read_num") val readNum: Int = 0,
    @SerialName("settings") val settings: GroupVoteSettings = GroupVoteSettings(),
    @SerialName("type") val type: Int = 0,
    @SerialName("u") val uid: Long = 0,
    @SerialName("vn") val vn: Int = 0
)

internal suspend fun QQAndroidBot.getGroupVoteList(
    groupCode: Long, limit: Int
): GroupVoteList {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://client.qun.qq.com/cgi-bin/feeds/get_t_list")

        parameter("qid", groupCode)
        parameter("s", -1)
        parameter("ft", 21)
        parameter("i", 1)
        parameter("n", limit)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey("qun.qq.com")};"
            )
        }
    }.bodyAsText().loadAs(GroupVoteList.serializer())
}

internal suspend fun QQAndroidBot.getGroupVote(
    groupCode: Long, fid: String
): GroupVote {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://client.qun.qq.com/cgi-bin/feeds/get_feed")

        parameter("fid", fid)
        parameter("qid", groupCode)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey("qun.qq.com")};"
            )
        }
    }.bodyAsText().loadAs(GroupVote.serializer())
}