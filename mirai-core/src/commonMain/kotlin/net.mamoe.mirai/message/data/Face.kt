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

import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * QQ 自带表情
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:face:*[id]*&#93;
 */
data class Face(val id: Int) : // used in delegation
    MessageContent, CodableMessage {

    override fun toString(): String = "[mirai:face:$id]"
    override fun contentToString(): String =
            if (id >= 0 && id <= 255)
                FaceName.names[id]
            else "[表情]"

    override fun equals(other: Any?): Boolean = other is Face && other.id == this.id
    override fun hashCode(): Int = id

    /**
     * @author LamGC
     */
    @Suppress("SpellCheckingInspection", "unused")
    companion object IdList : Message.Key<Face> {
        override val typeName: String
            get() = "Face"

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
        const val hexie: Int = 184
        const val yangtuo: Int = 185
        const val youling: Int = 187
        const val dan: Int = 188
        const val juhua: Int = 190
        const val hongbao: Int = 192
        const val daxiao: Int = 193
        const val bukaixin: Int = 194
        const val lengmo: Int = 197
        const val e: Int = 198
        const val haobang: Int = 199
        const val baituo: Int = 200
        const val dianzan: Int = 201
        const val wuliao: Int = 202
        const val tuolian: Int = 203
        const val chi: Int = 204
        const val songhua: Int = 205
        const val haipa: Int = 206
        const val huachi: Int = 207
        const val xiaoyanger: Int = 208
        const val biaolei: Int = 210
        const val wobukan: Int = 211
        const val bobo: Int = 214
        const val hulian: Int = 215
        const val paitou: Int = 216
        const val cheyiche: Int = 217
        const val tianyitian: Int = 218
        const val cengyiceng: Int = 219
        const val zhuaizhatian: Int = 220
        const val dingguagua: Int = 221
        const val baobao: Int = 222
        const val baoji: Int = 223
        const val kaiqiang: Int = 224
        const val liaoyiliao: Int = 225
        const val paizhuo: Int = 226
        const val paishou: Int = 227
        const val gongxi: Int = 228
        const val ganbei: Int = 229
        const val chaofeng: Int = 230
        const val heng: Int = 231
        const val foxi: Int = 232
        const val qiaoyiqioa: Int = 233
        const val jingdai: Int = 234
        const val chandou: Int = 235
        const val kentou: Int = 236
        const val toukan: Int = 237
        const val shanlian: Int = 238
        const val yuanliang: Int = 239
        const val penlian: Int = 240
        const val shengrikuaile: Int = 241
        const val touzhuangji: Int = 242
        const val shuaitou: Int = 243
        const val rengou: Int = 244
    }


    @PlannedRemoval("1.2.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @Suppress("unused", "UNUSED_PARAMETER")
    private constructor(id: Int, stringValue: String) : this(id)

    @JvmSynthetic
    @PlannedRemoval("1.2.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @Suppress("unused", "UNUSED_PARAMETER")
    fun copy(id: Int = this.id, stringValue: String = "") = this.copy(id = id)

    @JvmSynthetic
    @PlannedRemoval("1.2.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @Suppress("unused", "UNUSED_PARAMETER")
    operator fun component2(): String = toString()
}


/**
 *  @author Niltok
 */
@Suppress("SpellCheckingInspection")
private object FaceName {
    val names = Array<String>(256, { "[表情]" })

    init {
        names[Face.jingya] = "[惊讶]"
        names[Face.piezui] = "[撇嘴]"
        names[Face.se] = "[色]"
        names[Face.fadai] = "[发呆]"
        names[Face.deyi] = "[得意]"
        names[Face.liulei] = "[流泪]"
        names[Face.haixiu] = "[害羞]"
        names[Face.bizui] = "[闭嘴]"
        names[Face.shui] = "[睡]"
        names[Face.daku] = "[大哭]"
        names[Face.ganga] = "[尴尬]"
        names[Face.fanu] = "[发怒]"
        names[Face.tiaopi] = "[调皮]"
        names[Face.ciya] = "[呲牙]"
        names[Face.weixiao] = "[微笑]"
        names[Face.nanguo] = "[难过]"
        names[Face.ku] = "[酷]"
        names[Face.zhuakuang] = "[抓狂]"
        names[Face.tu] = "[吐]"
        names[Face.touxiao] = "[偷笑]"
        names[Face.keai] = "[可爱]"
        names[Face.baiyan] = "[白眼]"
        names[Face.aoman] = "[傲慢]"
        names[Face.ji_e] = "[饥饿]"
        names[Face.kun] = "[困]"
        names[Face.jingkong] = "[惊恐]"
        names[Face.liuhan] = "[流汗]"
        names[Face.hanxiao] = "[憨笑]"
        names[Face.dabing] = "[大病]"
        names[Face.fendou] = "[奋斗]"
        names[Face.zhouma] = "[咒骂]"
        names[Face.yiwen] = "[疑问]"
        names[Face.yun] = "[晕]"
        names[Face.zhemo] = "[折磨]"
        names[Face.shuai] = "[衰]"
        names[Face.kulou] = "[骷髅]"
        names[Face.qiaoda] = "[敲打]"
        names[Face.zaijian] = "[再见]"
        names[Face.fadou] = "[发抖]"
        names[Face.aiqing] = "[爱情]"
        names[Face.tiaotiao] = "[跳跳]"
        names[Face.zhutou] = "[猪头]"
        names[Face.yongbao] = "[拥抱]"
        names[Face.dan_gao] = "[蛋糕]"
        names[Face.shandian] = "[闪电]"
        names[Face.zhadan] = "[炸弹]"
        names[Face.dao] = "[刀]"
        names[Face.zuqiu] = "[足球]"
        names[Face.bianbian] = "[便便]"
        names[Face.kafei] = "[咖啡]"
        names[Face.fan] = "[饭]"
        names[Face.meigui] = "[玫瑰]"
        names[Face.diaoxie] = "[凋谢]"
        names[Face.aixin] = "[爱心]"
        names[Face.xinsui] = "[心碎]"
        names[Face.liwu] = "[礼物]"
        names[Face.taiyang] = "[太阳]"
        names[Face.yueliang] = "[月亮]"
        names[Face.qiang] = "[强]"
        names[Face.ruo] = "[弱]"
        names[Face.woshou] = "[握手]"
        names[Face.shengli] = "[胜利]"
        names[Face.feiwen] = "[飞吻]"
        names[Face.naohuo] = "[恼火]"
        names[Face.xigua] = "[西瓜]"
        names[Face.lenghan] = "[冷汗]"
        names[Face.cahan] = "[擦汗]"
        names[Face.koubi] = "[抠鼻]"
        names[Face.guzhang] = "[鼓掌]"
        names[Face.qiudale] = "[糗大了]"
        names[Face.huaixiao] = "[坏笑]"
        names[Face.zuohengheng] = "[左哼哼]"
        names[Face.youhengheng] = "[右哼哼]"
        names[Face.haqian] = "[哈欠]"
        names[Face.bishi] = "[鄙视]"
        names[Face.weiqu] = "[委屈]"
        names[Face.kuaikule] = "[快哭了]"
        names[Face.yinxian] = "[阴险]"
        names[Face.qinqin] = "[亲亲]"
        names[Face.xia] = "[吓]"
        names[Face.kelian] = "[可怜]"
        names[Face.caidao] = "[菜刀]"
        names[Face.pijiu] = "[啤酒]"
        names[Face.lanqiu] = "[篮球]"
        names[Face.pingpang] = "[乒乓]"
        names[Face.shiai] = "[示爱]"
        names[Face.piaochong] = "[瓢虫]"
        names[Face.baoquan] = "[抱拳]"
        names[Face.gouyin] = "[勾引]"
        names[Face.quantou] = "[拳头]"
        names[Face.chajin] = "[差劲]"
        names[Face.aini] = "[爱你]"
        names[Face.bu] = "[NO]"
        names[Face.hao] = "[OK]"
        names[Face.zhuanquan] = "[转圈]"
        names[Face.ketou] = "[磕头]"
        names[Face.huitou] = "[回头]"
        names[Face.tiaosheng] = "[跳绳]"
        names[Face.huishou] = "[挥手]"
        names[Face.jidong] = "[激动]"
        names[Face.jiewu] = "[街舞]"
        names[Face.xianwen] = "[献吻]"
        names[Face.zuotaiji] = "[左太极]"
        names[Face.youtaiji] = "[右太极]"
        names[Face.shuangxi] = "[双喜]"
        names[Face.bianpao] = "[鞭炮]"
        names[Face.denglong] = "[灯笼]"
        names[Face.facai] = "[发财]"
        names[Face.K_ge] = "[K歌]"
        names[Face.gouwu] = "[购物]"
        names[Face.youjian] = "[邮件]"
        names[Face.shuai_qi] = "[帅气]"
        names[Face.hecai] = "[喝彩]"
        names[Face.qidao] = "[祈祷]"
        names[Face.baojin] = "[爆筋]"
        names[Face.bangbangtang] = "[棒棒糖]"
        names[Face.he_nai] = "[喝奶]"
        names[Face.xiamian] = "[下面]"
        names[Face.xiangjiao] = "[香蕉]"
        names[Face.feiji] = "[飞机]"
        names[Face.kaiche] = "[开车]"
        names[Face.gaotiezuochetou] = "[高铁左车头]"
        names[Face.chexiang] = "[车厢]"
        names[Face.gaotieyouchetou] = "[高铁右车头]"
        names[Face.duoyun] = "[多云]"
        names[Face.xiayu] = "[下雨]"
        names[Face.chaopiao] = "[钞票]"
        names[Face.xiongmao] = "[熊猫]"
        names[Face.dengpao] = "[灯泡]"
        names[Face.fengche] = "[风车]"
        names[Face.naozhong] = "[闹钟]"
        names[Face.dasan] = "[打伞]"
        names[Face.caiqiu] = "[彩球]"
        names[Face.zuanjie] = "[钻戒]"
        names[Face.shafa] = "[沙发]"
        names[Face.zhijin] = "[纸巾]"
        names[Face.yao] = "[药]"
        names[Face.shouqiang] = "[手枪]"
        names[Face.qingwa] = "[青蛙]"
        names[Face.hexie] = "[河蟹]"
        names[Face.yangtuo] = "[羊驼]"
        names[Face.youling] = "[幽灵]"
        names[Face.dan] = "[蛋]"
        names[Face.juhua] = "[菊花]"
        names[Face.hongbao] = "[红包]"
        names[Face.daxiao] = "[大笑]"
        names[Face.bukaixin] = "[不开心]"
        names[Face.lengmo] = "[冷漠]"
        names[Face.e] = "[呃]"
        names[Face.haobang] = "[好棒]"
        names[Face.baituo] = "[拜托]"
        names[Face.dianzan] = "[点赞]"
        names[Face.wuliao] = "[无聊]"
        names[Face.tuolian] = "[托脸]"
        names[Face.chi] = "[吃]"
        names[Face.songhua] = "[送花]"
        names[Face.haipa] = "[害怕]"
        names[Face.huachi] = "[花痴]"
        names[Face.xiaoyanger] = "[小样儿]"
        names[Face.biaolei] = "[飙泪]"
        names[Face.wobukan] = "[我不看]"
        names[212] = "[托腮]"
        names[Face.bobo] = "[啵啵]"
        names[Face.hulian] = "[糊脸]"
        names[Face.paitou] = "[拍头]"
        names[Face.cheyiche] = "[扯一扯]"
        names[Face.tianyitian] = "[舔一舔]"
        names[Face.cengyiceng] = "[蹭一蹭]"
        names[Face.zhuaizhatian] = "[拽炸天]"
        names[Face.dingguagua] = "[顶呱呱]"
        names[Face.baobao] = "[抱抱]"
        names[Face.baoji] = "[暴击]"
        names[Face.kaiqiang] = "[开枪]"
        names[Face.liaoyiliao] = "[撩一撩]"
        names[Face.paizhuo] = "[拍桌]"
        names[Face.paishou] = "[拍手]"
        names[Face.gongxi] = "[恭喜]"
        names[Face.ganbei] = "[干杯]"
        names[Face.chaofeng] = "[嘲讽]"
        names[Face.heng] = "[哼]"
        names[Face.foxi] = "[佛系]"
        names[Face.qiaoyiqioa] = "[敲一敲]"
        names[Face.jingdai] = "[惊呆]"
        names[Face.chandou] = "[颤抖]"
        names[Face.kentou] = "[啃头]"
        names[Face.toukan] = "[偷看]"
        names[Face.shanlian] = "[扇脸]"
        names[Face.yuanliang] = "[原谅]"
        names[Face.penlian] = "[喷脸]"
        names[Face.shengrikuaile] = "[生日快乐]"
        names[Face.touzhuangji] = "[头撞击]"
        names[Face.shuaitou] = "[甩头]"
        names[Face.rengou] = "[扔狗]"
        names[245] = "[必胜加油]"
        names[246] = "[加油抱抱]"
        names[247] = "[口罩护体]"
    }
}