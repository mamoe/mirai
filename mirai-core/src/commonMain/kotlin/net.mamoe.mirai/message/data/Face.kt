/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
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
public data class Face(public val id: Int) : // used in delegation
    MessageContent, CodableMessage {

    public override fun toString(): String = "[mirai:face:$id]"
    public override fun contentToString(): String =
        if (id >= 0 && id <= 255)
            FaceName.names[id]
        else "[表情]"

    public override fun equals(other: Any?): Boolean = other is Face && other.id == this.id
    public override fun hashCode(): Int = id

    /**
     * @author LamGC
     */
    @Suppress("SpellCheckingInspection", "unused")
    public companion object IdList : Message.Key<Face> {
        public override val typeName: String
            get() = "Face"

        public const val unknown: Int = 0xff
        public const val jingya: Int = 0
        public const val piezui: Int = 1
        public const val se: Int = 2
        public const val fadai: Int = 3
        public const val deyi: Int = 4
        public const val liulei: Int = 5
        public const val haixiu: Int = 6
        public const val bizui: Int = 7
        public const val shui: Int = 8
        public const val daku: Int = 9
        public const val ganga: Int = 10
        public const val fanu: Int = 11
        public const val tiaopi: Int = 12
        public const val ciya: Int = 13
        public const val weixiao: Int = 14
        public const val nanguo: Int = 15
        public const val ku: Int = 16
        public const val zhuakuang: Int = 18
        public const val tu: Int = 19
        public const val touxiao: Int = 20
        public const val keai: Int = 21
        public const val baiyan: Int = 22
        public const val aoman: Int = 23
        public const val ji_e: Int = 24
        public const val kun: Int = 25
        public const val jingkong: Int = 26
        public const val liuhan: Int = 27
        public const val hanxiao: Int = 28
        public const val dabing: Int = 29
        public const val fendou: Int = 30
        public const val zhouma: Int = 31
        public const val yiwen: Int = 32
        public const val yun: Int = 34
        public const val zhemo: Int = 35
        public const val shuai: Int = 36
        public const val kulou: Int = 37
        public const val qiaoda: Int = 38
        public const val zaijian: Int = 39
        public const val fadou: Int = 41
        public const val aiqing: Int = 42
        public const val tiaotiao: Int = 43
        public const val zhutou: Int = 46
        public const val yongbao: Int = 49
        public const val dan_gao: Int = 53
        public const val shandian: Int = 54
        public const val zhadan: Int = 55
        public const val dao: Int = 56
        public const val zuqiu: Int = 57
        public const val bianbian: Int = 59
        public const val kafei: Int = 60
        public const val fan: Int = 61
        public const val meigui: Int = 63
        public const val diaoxie: Int = 64
        public const val aixin: Int = 66
        public const val xinsui: Int = 67
        public const val liwu: Int = 69
        public const val taiyang: Int = 74
        public const val yueliang: Int = 75
        public const val qiang: Int = 76
        public const val ruo: Int = 77
        public const val woshou: Int = 78
        public const val shengli: Int = 79
        public const val feiwen: Int = 85
        public const val naohuo: Int = 86
        public const val xigua: Int = 89
        public const val lenghan: Int = 96
        public const val cahan: Int = 97
        public const val koubi: Int = 98
        public const val guzhang: Int = 99
        public const val qiudale: Int = 100
        public const val huaixiao: Int = 101
        public const val zuohengheng: Int = 102
        public const val youhengheng: Int = 103
        public const val haqian: Int = 104
        public const val bishi: Int = 105
        public const val weiqu: Int = 106
        public const val kuaikule: Int = 107
        public const val yinxian: Int = 108
        public const val qinqin: Int = 109
        public const val xia: Int = 110
        public const val kelian: Int = 111
        public const val caidao: Int = 112
        public const val pijiu: Int = 113
        public const val lanqiu: Int = 114
        public const val pingpang: Int = 115
        public const val shiai: Int = 116
        public const val piaochong: Int = 117
        public const val baoquan: Int = 118
        public const val gouyin: Int = 119
        public const val quantou: Int = 120
        public const val chajin: Int = 121
        public const val aini: Int = 122
        public const val bu: Int = 123
        public const val hao: Int = 124
        public const val zhuanquan: Int = 125
        public const val ketou: Int = 126
        public const val huitou: Int = 127
        public const val tiaosheng: Int = 128
        public const val huishou: Int = 129
        public const val jidong: Int = 130
        public const val jiewu: Int = 131
        public const val xianwen: Int = 132
        public const val zuotaiji: Int = 133
        public const val youtaiji: Int = 134
        public const val shuangxi: Int = 136
        public const val bianpao: Int = 137
        public const val denglong: Int = 138
        public const val facai: Int = 139
        public const val K_ge: Int = 140
        public const val gouwu: Int = 141
        public const val youjian: Int = 142
        public const val shuai_qi: Int = 143
        public const val hecai: Int = 144
        public const val qidao: Int = 145
        public const val baojin: Int = 146
        public const val bangbangtang: Int = 147
        public const val he_nai: Int = 148
        public const val xiamian: Int = 149
        public const val xiangjiao: Int = 150
        public const val feiji: Int = 151
        public const val kaiche: Int = 152
        public const val gaotiezuochetou: Int = 153
        public const val chexiang: Int = 154
        public const val gaotieyouchetou: Int = 155
        public const val duoyun: Int = 156
        public const val xiayu: Int = 157
        public const val chaopiao: Int = 158
        public const val xiongmao: Int = 159
        public const val dengpao: Int = 160
        public const val fengche: Int = 161
        public const val naozhong: Int = 162
        public const val dasan: Int = 163
        public const val caiqiu: Int = 164
        public const val zuanjie: Int = 165
        public const val shafa: Int = 166
        public const val zhijin: Int = 167
        public const val yao: Int = 168
        public const val shouqiang: Int = 169
        public const val qingwa: Int = 170
        public const val hexie: Int = 184
        public const val yangtuo: Int = 185
        public const val youling: Int = 187
        public const val dan: Int = 188
        public const val juhua: Int = 190
        public const val hongbao: Int = 192
        public const val daxiao: Int = 193
        public const val bukaixin: Int = 194
        public const val lengmo: Int = 197
        public const val e: Int = 198
        public const val haobang: Int = 199
        public const val baituo: Int = 200
        public const val dianzan: Int = 201
        public const val wuliao: Int = 202
        public const val tuolian: Int = 203
        public const val chi: Int = 204
        public const val songhua: Int = 205
        public const val haipa: Int = 206
        public const val huachi: Int = 207
        public const val xiaoyanger: Int = 208
        public const val biaolei: Int = 210
        public const val wobukan: Int = 211
        public const val bobo: Int = 214
        public const val hulian: Int = 215
        public const val paitou: Int = 216
        public const val cheyiche: Int = 217
        public const val tianyitian: Int = 218
        public const val cengyiceng: Int = 219
        public const val zhuaizhatian: Int = 220
        public const val dingguagua: Int = 221
        public const val baobao: Int = 222
        public const val baoji: Int = 223
        public const val kaiqiang: Int = 224
        public const val liaoyiliao: Int = 225
        public const val paizhuo: Int = 226
        public const val paishou: Int = 227
        public const val gongxi: Int = 228
        public const val ganbei: Int = 229
        public const val chaofeng: Int = 230
        public const val heng: Int = 231
        public const val foxi: Int = 232
        public const val qiaoyiqioa: Int = 233
        public const val jingdai: Int = 234
        public const val chandou: Int = 235
        public const val kentou: Int = 236
        public const val toukan: Int = 237
        public const val shanlian: Int = 238
        public const val yuanliang: Int = 239
        public const val penlian: Int = 240
        public const val shengrikuaile: Int = 241
        public const val touzhuangji: Int = 242
        public const val shuaitou: Int = 243
        public const val rengou: Int = 244
    }


    @PlannedRemoval("1.2.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @Suppress("unused", "UNUSED_PARAMETER")
    private constructor(id: Int, stringValue: String) : this(id)

    @JvmSynthetic
    @PlannedRemoval("1.2.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @Suppress("unused", "UNUSED_PARAMETER")
    public fun copy(id: Int = this.id, stringValue: String = ""): Face = this.copy(id = id)

    @JvmSynthetic
    @PlannedRemoval("1.2.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @Suppress("unused", "UNUSED_PARAMETER")
    public operator fun component2(): String = toString()
}


/**
 *  @author Niltok
 */
@Suppress("SpellCheckingInspection")
private object FaceName {
    val names = Array(256) { "[表情]" }

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