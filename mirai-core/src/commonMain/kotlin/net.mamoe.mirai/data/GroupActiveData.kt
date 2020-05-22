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
    val role: Int?
) {
    @Serializable
    data class GInfo(


        @SerialName("g_act_num")
        val actNum: List<GActNum?>?,    //发言人数列表

        @SerialName("g_createtime")
        val createTime: Int?,

        @SerialName("g_exit_num")
        val exitNum: List<GExitNum?>?,  //退群人数列表

        @SerialName("g_join_num")
        val joinNum: List<GJoinNum?>?,

        @SerialName("g_mem_num")
        val memNum: List<GMemNum?>?,   //人数变化

        @SerialName("g_most_act")
        val mostAct: List<GMostAct?>?,  //发言排行

        @SerialName("g_sentences")
        val sentences: List<GSentence?>?,

        @SerialName("gc")
        val gc: Int?,

        @SerialName("gn")
        val gn: String?,

        @SerialName("gowner")
        val gowner: String?,

        @SerialName("isEnd")
        val isEnd: Int?
    ) {
        @Serializable
        data class GActNum(

            @SerialName("date")
            val date: String?,

            @SerialName("num")
            val num: Int?
        )

        @Serializable
        data class GExitNum(

            @SerialName("date")
            val date: String?,

            @SerialName("num")
            val num: Int?
        )

        @Serializable
        data class GJoinNum(

            @SerialName("date")
            val date: String?,

            @SerialName("num")
            val num: Int?
        )

        @Serializable
        data class GMemNum(

            @SerialName("date")
            val date: String?,

            @SerialName("num")
            val num: Int?
        )

        @Serializable
        data class GMostAct(

            @SerialName("name")
            val name: String?,  // 名称 不完整

            @SerialName("sentences_num")
            val sentencesNum: Int?,   // 发言数

            @SerialName("sta")
            val sta: Int?,

            @SerialName("uin")
            val uin: Long?
        )

        @Serializable
        data class GSentence(

            @SerialName("date")
            val date: String?,

            @SerialName("num")
            val num: Int?
        )
    }
}