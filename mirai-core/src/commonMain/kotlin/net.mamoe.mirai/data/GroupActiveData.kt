/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.MiraiExperimentalAPI


/**
 * 群统计信息
 */
@MiraiExperimentalAPI
@Serializable
public data class GroupActiveData(

    @SerialName("ec")
    val ec: Int? = null,

    @SerialName("em")
    val msg: String?,

    @SerialName("errcode")
    val errCode: Int?,

    @SerialName("ginfo")
    val info: GInfo? = null,

    @SerialName("role")
    val role: Int? = 0
) {
    @Serializable
    public data class GInfo(


        @SerialName("g_act_num")
        val actNum: List<GActNum?>? = null,    //发言人数列表

        @SerialName("g_createtime")
        val createTime: Int? = 0,

        @SerialName("g_exit_num")
        val exitNum: List<GExitNum?>? = null,  //退群人数列表

        @SerialName("g_join_num")
        val joinNum: List<GJoinNum?>? = null,

        @SerialName("g_mem_num")
        val memNum: List<GMemNum?>? = null,   //人数变化

        @SerialName("g_most_act")
        val mostAct: List<GMostAct?>? = null,  //发言排行

        @SerialName("g_sentences")
        val sentences: List<GSentence?>? = null,

        @SerialName("gc")
        val gc: Int? = null,

        @SerialName("gn")
        val gn: String? = null,

        @SerialName("gowner")
        val gowner: String? = null,

        @SerialName("isEnd")
        val isEnd: Int? = null
    ) {
        @Serializable
        public data class GActNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        public data class GExitNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        public data class GJoinNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        public data class GMemNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        public data class GMostAct(

            @SerialName("name")
            val name: String? = null,  // 名称 不完整

            @SerialName("sentences_num")
            val sentencesNum: Int? = 0,   // 发言数

            @SerialName("sta")
            val sta: Int? = 0,

            @SerialName("uin")
            val uin: Long? = 0
        )

        @Serializable
        public data class GSentence(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )
    }
}