/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * QQ 自带表情
 */
class Face(val id: Int) : Message {
    override fun toString(): String = "[mirai:face$id]"

    /**
     * @author LamGC
     */
    @Suppress("SpellCheckingInspection", "unused")
    companion object IdList : Message.Key<Face> {
        const val unknown: Int = 0xff
        const val jingya: Int = 0
        const val piezui: Int = 1
        const val se: Int = 2
        const val fadai: Int = 3
        const val deyi: Int = 4
        const val liulei: Int = 5
        const val haixiu: Int = 6
        const val bizui: Int = 7
        const val shui: Int = 8
        const val daku: Int = 9
        const val ganga: Int = 10
        const val fanu: Int = 11
        const val tiaopi: Int = 12
        const val ciya: Int = 13
        const val weixiao: Int = 14
        const val nanguo: Int = 15
        const val ku: Int = 16
        const val zhuakuang: Int = 18
        const val tu: Int = 19
        const val touxiao: Int = 20
        const val keai: Int = 21
        const val baiyan: Int = 22
        const val aoman: Int = 23
        const val ji_e: Int = 24
        const val kun: Int = 25
        const val jingkong: Int = 26
        const val liuhan: Int = 27
        const val hanxiao: Int = 28
        const val dabing: Int = 29
        const val fendou: Int = 30
        const val zhouma: Int = 31
        const val yiwen: Int = 32
        const val yun: Int = 34
        const val zhemo: Int = 35
        const val shuai: Int = 36
        const val kulou: Int = 37
        const val qiaoda: Int = 38
        const val zaijian: Int = 39
        const val fadou: Int = 41
        const val aiqing: Int = 42
        const val tiaotiao: Int = 43
        const val zhutou: Int = 46
        const val yongbao: Int = 49
        const val dan_gao: Int = 53
        const val shandian: Int = 54
        const val zhadan: Int = 55
        const val dao: Int = 56
        const val zuqiu: Int = 57
        const val bianbian: Int = 59
        const val kafei: Int = 60
        const val fan: Int = 61
        const val meigui: Int = 63
        const val diaoxie: Int = 64
        const val aixin: Int = 66
        const val xinsui: Int = 67
        const val liwu: Int = 69
        const val taiyang: Int = 74
        const val yueliang: Int = 75
        const val qiang: Int = 76
        const val ruo: Int = 77
        const val woshou: Int = 78
        const val shengli: Int = 79
        const val feiwen: Int = 85
        const val naohuo: Int = 86
        const val xigua: Int = 89
        const val lenghan: Int = 96
        const val cahan: Int = 97
        const val koubi: Int = 98
        const val guzhang: Int = 99
        const val qiudale: Int = 100
        const val huaixiao: Int = 101
        const val zuohengheng: Int = 102
        const val youhengheng: Int = 103
        const val haqian: Int = 104
        const val bishi: Int = 105
        const val weiqu: Int = 106
        const val kuaikule: Int = 107
        const val yinxian: Int = 108
        const val qinqin: Int = 109
        const val xia: Int = 110
        const val kelian: Int = 111
        const val caidao: Int = 112
        const val pijiu: Int = 113
        const val lanqiu: Int = 114
        const val pingpang: Int = 115
        const val shiai: Int = 116
        const val piaochong: Int = 117
        const val baoquan: Int = 118
        const val gouyin: Int = 119
        const val quantou: Int = 120
        const val chajin: Int = 121
        const val aini: Int = 122
        const val bu: Int = 123
        const val hao: Int = 124
        const val zhuanquan: Int = 125
        const val ketou: Int = 126
        const val huitou: Int = 127
        const val tiaosheng: Int = 128
        const val huishou: Int = 129
        const val jidong: Int = 130
        const val jiewu: Int = 131
        const val xianwen: Int = 132
        const val zuotaiji: Int = 133
        const val youtaiji: Int = 134
        const val shuangxi: Int = 136
        const val bianpao: Int = 137
        const val denglong: Int = 138
        const val facai: Int = 139
        const val K_ge: Int = 140
        const val gouwu: Int = 141
        const val youjian: Int = 142
        const val shuai_qi: Int = 143
        const val hecai: Int = 144
        const val qidao: Int = 145
        const val baojin: Int = 146
        const val bangbangtang: Int = 147
        const val he_nai: Int = 148
        const val xiamian: Int = 149
        const val xiangjiao: Int = 150
        const val feiji: Int = 151
        const val kaiche: Int = 152
        const val gaotiezuochetou: Int = 153
        const val chexiang: Int = 154
        const val gaotieyouchetou: Int = 155
        const val duoyun: Int = 156
        const val xiayu: Int = 157
        const val chaopiao: Int = 158
        const val xiongmao: Int = 159
        const val dengpao: Int = 160
        const val fengche: Int = 161
        const val naozhong: Int = 162
        const val dasan: Int = 163
        const val caiqiu: Int = 164
        const val zuanjie: Int = 165
        const val shafa: Int = 166
        const val zhijin: Int = 167
        const val yao: Int = 168
        const val shouqiang: Int = 169
        const val qingwa: Int = 170
    }
}