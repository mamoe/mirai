/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.essence

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.active.defaultJson
import net.mamoe.mirai.internal.network.components.HttpClientProvider
import net.mamoe.mirai.internal.network.psKey
import net.mamoe.mirai.internal.network.sKey
import net.mamoe.mirai.utils.CheckableResponseA
import net.mamoe.mirai.utils.JsonStruct
import net.mamoe.mirai.utils.loadAs

@Serializable
internal data class DigestData(
    @SerialName("data") val `data`: JsonElement = JsonNull,
    @SerialName("wording") val reason: String = "",
    @SerialName("retmsg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct

@Serializable
internal data class DigestList(
    @SerialName("group_role")
    val role: Int = 0,
    @SerialName("is_end")
    val isEnd: Boolean = false,
    @SerialName("msg_list")
    val messages: List<DigestMessage> = emptyList(),
    @SerialName("show_tips")
    val showTips: Boolean = false
)

@Serializable
internal data class DigestMessage(
    @SerialName("add_digest_nick")
    val addDigestNick: String = "",
    @SerialName("add_digest_time")
    val addDigestTime: Int = 0,
    @SerialName("add_digest_uin")
    val addDigestUin: Long = 0,
    @SerialName("group_code")
    val groupCode: String = "",
    @SerialName("msg_content")
    val msgContent: List<DigestMessageContent> = emptyList(),
    @SerialName("msg_random")
    val msgRandom: Int = 0,
    @SerialName("msg_seq")
    val msgSeq: Int = 0,
    @SerialName("sender_nick")
    val senderNick: String = "",
    @SerialName("sender_time")
    val senderTime: Int = 0,
    @SerialName("sender_uin")
    val senderUin: Long = 0
)

@Serializable
internal data class DigestMessageContent(
    @SerialName("image_thumbnail_url")
    val imageThumbnailUrl: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    @SerialName("msg_type")
    val msgType: Int = 0,
    @SerialName("text")
    val text: String = ""
)

@Serializable
internal data class DigestShare(
    @SerialName("share_key")
    val shareKey: String = ""
)

private fun <T> DigestData.loadData(serializer: KSerializer<T>): T {
    return try {
        defaultJson.decodeFromJsonElement(serializer, this.data)
    } catch (cause: Exception) {
        throw IllegalStateException("parse digest data error, status: $errorCode - $errorMessage", cause)
    }
}

internal suspend fun QQAndroidBot.getDigestList(
    groupCode: Long, pageStart: Int, pageLimit: Int
): DigestList {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/group_digest/digest_list")
        parameter("group_code", groupCode)
        parameter("page_start", pageStart)
        parameter("page_limit", pageLimit)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadAs(DigestData.serializer()).loadData(DigestList.serializer())
}

internal suspend fun QQAndroidBot.cancelDigest(
    groupCode: Long, msgSeq: Int, msgRandom: Int
) {
    val data = components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/group_digest/cancel_digest")
        parameter("group_code", groupCode)
        parameter("msg_seq", msgSeq)
        parameter("msg_random", msgRandom)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadAs(DigestData.serializer())

    when (data.errorCode) {
        0, 11007, 11001 -> Unit
        else -> throw IllegalStateException("cancel digest error, status: ${data.errorCode} - ${data.errorMessage}, reason: ${data.reason}")
    }
}


internal suspend fun QQAndroidBot.shareDigest(
    groupCode: Long, msgSeq: Int, msgRandom: Int, targetGroupCode: Long
): DigestShare {
    return components[HttpClientProvider].getHttpClient().get {
        url("https://qun.qq.com/cgi-bin/group_digest/share_digest")
        parameter("group_code", groupCode)
        parameter("msg_seq", msgSeq)
        parameter("msg_random", msgRandom)
        parameter("target_group_code", targetGroupCode)
        parameter("bkn", client.wLoginSigInfo.bkn)

        headers {
            // ktor bug
            append(
                "cookie",
                "uin=o${id}; skey=${sKey}; p_uin=o${id}; p_skey=${psKey(host)};"
            )
        }
    }.bodyAsText().loadAs(DigestData.serializer()).loadData(DigestShare.serializer())
}