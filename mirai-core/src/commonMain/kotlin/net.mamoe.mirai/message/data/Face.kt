package net.mamoe.mirai.message.data

import kotlin.jvm.JvmStatic

/**
 * QQ 自带表情
 */
inline class Face(val id: FaceId) : Message {
    override fun toString(): String = "[face${id.value}]"

    companion object Key : Message.Key<Face>
}

/**
 * @author LamGC
 */
@Suppress("SpellCheckingInspection", "unused")
@UseExperimental(ExperimentalUnsignedTypes::class)
inline class FaceId constructor(inline val value: UByte) {
    companion object {
        @JvmStatic
        val unknown: FaceId = FaceId(0xffu)
        @JvmStatic
        val jingya: FaceId = FaceId(0u)
        @JvmStatic
        val piezui: FaceId = FaceId(1u)
        @JvmStatic
        val se: FaceId = FaceId(2u)
        @JvmStatic
        val fadai: FaceId = FaceId(3u)
        @JvmStatic
        val deyi: FaceId = FaceId(4u)
        @JvmStatic
        val liulei: FaceId = FaceId(5u)
        @JvmStatic
        val haixiu: FaceId = FaceId(6u)
        @JvmStatic
        val bizui: FaceId = FaceId(7u)
        @JvmStatic
        val shui: FaceId = FaceId(8u)
        @JvmStatic
        val daku: FaceId = FaceId(9u)
        @JvmStatic
        val ganga: FaceId = FaceId(10u)
        @JvmStatic
        val fanu: FaceId = FaceId(11u)
        @JvmStatic
        val tiaopi: FaceId = FaceId(12u)
        @JvmStatic
        val ciya: FaceId = FaceId(13u)
        @JvmStatic
        val weixiao: FaceId = FaceId(14u)
        @JvmStatic
        val nanguo: FaceId = FaceId(15u)
        @JvmStatic
        val ku: FaceId = FaceId(16u)
        @JvmStatic
        val zhuakuang: FaceId = FaceId(18u)
        @JvmStatic
        val tu: FaceId = FaceId(19u)
        @JvmStatic
        val touxiao: FaceId = FaceId(20u)
        @JvmStatic
        val keai: FaceId = FaceId(21u)
        @JvmStatic
        val baiyan: FaceId = FaceId(22u)
        @JvmStatic
        val aoman: FaceId = FaceId(23u)
        @JvmStatic
        val ji_e: FaceId = FaceId(24u)
        @JvmStatic
        val kun: FaceId = FaceId(25u)
        @JvmStatic
        val jingkong: FaceId = FaceId(26u)
        @JvmStatic
        val liuhan: FaceId = FaceId(27u)
        @JvmStatic
        val hanxiao: FaceId = FaceId(28u)
        @JvmStatic
        val dabing: FaceId = FaceId(29u)
        @JvmStatic
        val fendou: FaceId = FaceId(30u)
        @JvmStatic
        val zhouma: FaceId = FaceId(31u)
        @JvmStatic
        val yiwen: FaceId = FaceId(32u)
        @JvmStatic
        val yun: FaceId = FaceId(34u)
        @JvmStatic
        val zhemo: FaceId = FaceId(35u)
        @JvmStatic
        val shuai: FaceId = FaceId(36u)
        @JvmStatic
        val kulou: FaceId = FaceId(37u)
        @JvmStatic
        val qiaoda: FaceId = FaceId(38u)
        @JvmStatic
        val zaijian: FaceId = FaceId(39u)
        @JvmStatic
        val fadou: FaceId = FaceId(41u)
        @JvmStatic
        val aiqing: FaceId = FaceId(42u)
        @JvmStatic
        val tiaotiao: FaceId = FaceId(43u)
        @JvmStatic
        val zhutou: FaceId = FaceId(46u)
        @JvmStatic
        val yongbao: FaceId = FaceId(49u)
        @JvmStatic
        val dan_gao: FaceId = FaceId(53u)
        @JvmStatic
        val shandian: FaceId = FaceId(54u)
        @JvmStatic
        val zhadan: FaceId = FaceId(55u)
        @JvmStatic
        val dao: FaceId = FaceId(56u)
        @JvmStatic
        val zuqiu: FaceId = FaceId(57u)
        @JvmStatic
        val bianbian: FaceId = FaceId(59u)
        @JvmStatic
        val kafei: FaceId = FaceId(60u)
        @JvmStatic
        val fan: FaceId = FaceId(61u)
        @JvmStatic
        val meigui: FaceId = FaceId(63u)
        @JvmStatic
        val diaoxie: FaceId = FaceId(64u)
        @JvmStatic
        val aixin: FaceId = FaceId(66u)
        @JvmStatic
        val xinsui: FaceId = FaceId(67u)
        @JvmStatic
        val liwu: FaceId = FaceId(69u)
        @JvmStatic
        val taiyang: FaceId = FaceId(74u)
        @JvmStatic
        val yueliang: FaceId = FaceId(75u)
        @JvmStatic
        val qiang: FaceId = FaceId(76u)
        @JvmStatic
        val ruo: FaceId = FaceId(77u)
        @JvmStatic
        val woshou: FaceId = FaceId(78u)
        @JvmStatic
        val shengli: FaceId = FaceId(79u)
        @JvmStatic
        val feiwen: FaceId = FaceId(85u)
        @JvmStatic
        val naohuo: FaceId = FaceId(86u)
        @JvmStatic
        val xigua: FaceId = FaceId(89u)
        @JvmStatic
        val lenghan: FaceId = FaceId(96u)
        @JvmStatic
        val cahan: FaceId = FaceId(97u)
        @JvmStatic
        val koubi: FaceId = FaceId(98u)
        @JvmStatic
        val guzhang: FaceId = FaceId(99u)
        @JvmStatic
        val qiudale: FaceId = FaceId(100u)
        @JvmStatic
        val huaixiao: FaceId = FaceId(101u)
        @JvmStatic
        val zuohengheng: FaceId = FaceId(102u)
        @JvmStatic
        val youhengheng: FaceId = FaceId(103u)
        @JvmStatic
        val haqian: FaceId = FaceId(104u)
        @JvmStatic
        val bishi: FaceId = FaceId(105u)
        @JvmStatic
        val weiqu: FaceId = FaceId(106u)
        @JvmStatic
        val kuaikule: FaceId = FaceId(107u)
        @JvmStatic
        val yinxian: FaceId = FaceId(108u)
        @JvmStatic
        val qinqin: FaceId = FaceId(109u)
        @JvmStatic
        val xia: FaceId = FaceId(110u)
        @JvmStatic
        val kelian: FaceId = FaceId(111u)
        @JvmStatic
        val caidao: FaceId = FaceId(112u)
        @JvmStatic
        val pijiu: FaceId = FaceId(113u)
        @JvmStatic
        val lanqiu: FaceId = FaceId(114u)
        @JvmStatic
        val pingpang: FaceId = FaceId(115u)
        @JvmStatic
        val shiai: FaceId = FaceId(116u)
        @JvmStatic
        val piaochong: FaceId = FaceId(117u)
        @JvmStatic
        val baoquan: FaceId = FaceId(118u)
        @JvmStatic
        val gouyin: FaceId = FaceId(119u)
        @JvmStatic
        val quantou: FaceId = FaceId(120u)
        @JvmStatic
        val chajin: FaceId = FaceId(121u)
        @JvmStatic
        val aini: FaceId = FaceId(122u)
        @JvmStatic
        val bu: FaceId = FaceId(123u)
        @JvmStatic
        val hao: FaceId = FaceId(124u)
        @JvmStatic
        val zhuanquan: FaceId = FaceId(125u)
        @JvmStatic
        val ketou: FaceId = FaceId(126u)
        @JvmStatic
        val huitou: FaceId = FaceId(127u)
        @JvmStatic
        val tiaosheng: FaceId = FaceId(128u)
        @JvmStatic
        val huishou: FaceId = FaceId(129u)
        @JvmStatic
        val jidong: FaceId = FaceId(130u)
        @JvmStatic
        val jiewu: FaceId = FaceId(131u)
        @JvmStatic
        val xianwen: FaceId = FaceId(132u)
        @JvmStatic
        val zuotaiji: FaceId = FaceId(133u)
        @JvmStatic
        val youtaiji: FaceId = FaceId(134u)
        @JvmStatic
        val shuangxi: FaceId = FaceId(136u)
        @JvmStatic
        val bianpao: FaceId = FaceId(137u)
        @JvmStatic
        val denglong: FaceId = FaceId(138u)
        @JvmStatic
        val facai: FaceId = FaceId(139u)
        @JvmStatic
        val K_ge: FaceId = FaceId(140u)
        @JvmStatic
        val gouwu: FaceId = FaceId(141u)
        @JvmStatic
        val youjian: FaceId = FaceId(142u)
        @JvmStatic
        val shuai_qi: FaceId = FaceId(143u)
        @JvmStatic
        val hecai: FaceId = FaceId(144u)
        @JvmStatic
        val qidao: FaceId = FaceId(145u)
        @JvmStatic
        val baojin: FaceId = FaceId(146u)
        @JvmStatic
        val bangbangtang: FaceId = FaceId(147u)
        @JvmStatic
        val he_nai: FaceId = FaceId(148u)
        @JvmStatic
        val xiamian: FaceId = FaceId(149u)
        @JvmStatic
        val xiangjiao: FaceId = FaceId(150u)
        @JvmStatic
        val feiji: FaceId = FaceId(151u)
        @JvmStatic
        val kaiche: FaceId = FaceId(152u)
        @JvmStatic
        val gaotiezuochetou: FaceId = FaceId(153u)
        @JvmStatic
        val chexiang: FaceId = FaceId(154u)
        @JvmStatic
        val gaotieyouchetou: FaceId = FaceId(155u)
        @JvmStatic
        val duoyun: FaceId = FaceId(156u)
        @JvmStatic
        val xiayu: FaceId = FaceId(157u)
        @JvmStatic
        val chaopiao: FaceId = FaceId(158u)
        @JvmStatic
        val xiongmao: FaceId = FaceId(159u)
        @JvmStatic
        val dengpao: FaceId = FaceId(160u)
        @JvmStatic
        val fengche: FaceId = FaceId(161u)
        @JvmStatic
        val naozhong: FaceId = FaceId(162u)
        @JvmStatic
        val dasan: FaceId = FaceId(163u)
        @JvmStatic
        val caiqiu: FaceId = FaceId(164u)
        @JvmStatic
        val zuanjie: FaceId = FaceId(165u)
        @JvmStatic
        val shafa: FaceId = FaceId(166u)
        @JvmStatic
        val zhijin: FaceId = FaceId(167u)
        @JvmStatic
        val yao: FaceId = FaceId(168u)
        @JvmStatic
        val shouqiang: FaceId = FaceId(169u)
        @JvmStatic
        val qingwa: FaceId = FaceId(170u)
    }

    override fun toString(): String = "$FaceId($value)"
}
