package net.mamoe.mirai.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.MiraiExperimentalAPI


/**
 * 群统计信息
 */
@MiraiExperimentalAPI
@Serializable
data class GroupActiveData(

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
    data class GInfo(


        @SerialName("g_act_num")
        val actNum: List<GActNum?>? = null,    //发言人数列表

        @SerialName("g_createtime")
        val createTime: Int? = 0,

        @SerialName("g_exit_num")
        val exitNum: List<GExitNum?>? =  null,  //退群人数列表

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
        data class GActNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        data class GExitNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        data class GJoinNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        data class GMemNum(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )

        @Serializable
        data class GMostAct(

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
        data class GSentence(

            @SerialName("date")
            val date: String? = null,

            @SerialName("num")
            val num: Int? = 0
        )
    }
}