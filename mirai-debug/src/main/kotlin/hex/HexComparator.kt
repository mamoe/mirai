@file:Suppress("ObjectPropertyName", "MayBeConstant", "NonAsciiCharacters", "SpellCheckingInspection", "unused", "EXPERIMENTAL_UNSIGNED_LITERALS")

package hex

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.readUVarInt
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.math.max
import kotlin.reflect.KProperty0

/**
 * 匹配已知 hex 常量并格式化后打印到控制台.
 *
 * 低效率, 仅调试使用.
 */
internal fun String.printColorize(ignoreUntilFirstConst: Boolean): String = with(HexComparator) { colorize(ignoreUntilFirstConst) }

/**
 * 比较两个 hex 并格式化后打印到控制台.
 *
 * 低效率, 仅调试使用.
 */
@MiraiInternalAPI
fun printCompareHex(hex1s: String, hex2s: String): String = with(HexComparator) {
    compare(
        hex1s.toUpperCase(),
        hex2s.toUpperCase()
    )
}

data class NamedHexElement(
    val name: String,
    val value: String
)

/**
 * 初始化用于匹配的 Hex 列表
 */
private fun LinkedHashSet<NamedHexElement>.initConstFileds() {
    listOf(
        TestConsts,
        //TIMProtocol,
        PacketIds
    ).forEach { obj ->
        obj::class.members.filterIsInstance<KProperty0<*>>().forEach { property ->
            property.get()?.let { add(NamedHexElement(property.name, it.toString())) }
        }
    }
}

private object TestConsts {
    val NIU_BI = "牛逼".toByteArray().toUHexString()
    val _1994701021 = 1994701021u.toUHexString(" ")
    val _1040400290 = 1040400290u.toUHexString(" ")
    val _580266363 = 580266363u.toUHexString(" ")
    val _761025446 = 761025446u.toUHexString()

    val _1040400290_ = "3E 03 3F A2"
    val _1994701021_ = "76 E4 B8 DD"
    val _jiahua_ = "B1 89 BE 09"
    val _Him188moe_ = "Him188moe".toByteArray().toUHexString()
    val 发图片2 = "发图片2".toByteArray().toUHexString()
    val 发图片群 = "发图片群".toByteArray().toUHexString()
    val 发图片 = "发图片".toByteArray().toUHexString()
    val 群 = "群".toByteArray().toUHexString()
    val 你好 = "你好".toByteArray().toUHexString()

    val MESSAGE_TAIL_10404 =
        "0E  00  07  01  00  04  00  00  00  09 19  00  18  01  00  15  AA  02  12  9A  01  0F  80  01  01  C8  01  00  F0  01  00  F8  01  00  90  02  00"
            .replace("  ", " ")

    val FONT_10404 = "E5 BE AE E8 BD AF E9 9B 85 E9 BB 91"

    val varint580266363 = "FB D2 D8 94 02"
    val varint1040400290 = "A2 FF 8C F0 03"
    val varint1994701021 = "DD F1 92 B7 07"
    val varint761025446 = 761025446u.toUHexString().hexToBytes().read { readUVarInt() }.toUHexString()
}

@Suppress("SpellCheckingInspection")
private object PacketIds {
    val heartbeat = "00 58"
    val friendmsgsend = "00 CD"
    val friendmsgevent = "00 CE"
    val groupmsg = "00 02"
}

/**
 * Hex 比较器, 并着色已知常量
 *
 * This could be used to check packet encoding..
 * but better to run under UNIX
 *
 * @author NaturalHG
 * @author Him188moe
 */
private object HexComparator {

    private val RED = "\u001b[31m"
    private val GREEN = "\u001b[33m"
    private val UNKNOWN_COLOR = "\u001b[30m"
    private val BLUE = "\u001b[34m"

    @Suppress("unused")
    private class ConstMatcher constructor(hex: String) {
        private val matches = linkedSetOf<Match>()

        fun getMatchedConstName(hexNumber: Int): String? {
            for (match in this.matches) {
                if (match.range.contains(hexNumber)) {
                    return match.constName
                }
            }
            return null
        }

        private class Match internal constructor(val range: IntRange, val constName: String)

        init {
            CONST_FIELDS.forEach { (name, value) ->
                for (match in match(hex, value)) {
                    matches.add(Match(match, name))
                }
            }
        }

        companion object {
            val CONST_FIELDS: MutableSet<NamedHexElement> = linkedSetOf<NamedHexElement>().apply { initConstFileds() }
        }

        private fun match(hex: String, value: String): Set<IntRange> {
            val constValue: String
            try {
                constValue = value.trim { it <= ' ' }
                if (constValue.length / 3 <= 3) {//Minimum numbers of const hex bytes
                    return linkedSetOf()
                }
            } catch (ignored: ClassCastException) {
                return linkedSetOf()
            }

            return mutableSetOf<IntRange>().apply {
                var index = -1
                index = hex.indexOf(constValue, index + 1)
                while (index != -1) {
                    add(IntRange(index / 3, (index + constValue.length) / 3))

                    index = hex.indexOf(constValue, index + 1)
                }
            }
        }
    }

    private fun buildConstNameChain(length: Int, constMatcher: ConstMatcher, constNameBuilder: StringBuilder) {
        //System.out.println(constMatcher.matches);
        var i = 0
        while (i < length) {
            constNameBuilder.append(" ")
            val match = constMatcher.getMatchedConstName(i / 4)
            if (match != null) {
                var appendedNameLength = match.length
                constNameBuilder.append(match)
                while (match == constMatcher.getMatchedConstName(i++ / 4)) {
                    if (appendedNameLength-- < 0) {
                        constNameBuilder.append(" ")
                    }
                }

                constNameBuilder.append(" ".repeat(match.length % 4))
            }
            i++
        }
    }

    fun compare(hex1s: String, hex2s: String): String {
        val builder = StringBuilder()

        val hex1 = hex1s.trim { it <= ' ' }.replace("\n", "").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val hex2 = hex2s.trim { it <= ' ' }.replace("\n", "").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val constMatcher1 = ConstMatcher(hex1s)
        val constMatcher2 = ConstMatcher(hex2s)

        if (hex1.size == hex2.size) {
            builder.append(GREEN).append("长度一致:").append(hex1.size)
        } else {
            builder.append(RED).append("长度不一致").append(hex1.size).append("/").append(hex2.size)
        }


        val numberLine = StringBuilder()
        val hex1ConstName = StringBuilder()
        val hex1b = StringBuilder()
        val hex2b = StringBuilder()
        val hex2ConstName = StringBuilder()
        var dif = 0

        val length = max(hex1.size, hex2.size) * 4
        buildConstNameChain(length, constMatcher1, hex1ConstName)
        buildConstNameChain(length, constMatcher2, hex2ConstName)


        for (i in 0 until max(hex1.size, hex2.size)) {
            var h1: String? = null
            var h2: String? = null
            var isDif = false
            if (hex1.size <= i) {
                h1 = RED + "__"
                isDif = true
            } else {
                val matchedConstName = constMatcher1.getMatchedConstName(i)
                if (matchedConstName != null) {
                    h1 = BLUE + hex1[i]
                }
            }
            if (hex2.size <= i) {
                h2 = RED + "__"
                isDif = true
            } else {
                val matchedConstName = constMatcher2.getMatchedConstName(i)
                if (matchedConstName != null) {
                    h2 = BLUE + hex2[i]
                }
            }

            if (h1 == null && h2 == null) {
                h1 = hex1[i]
                h2 = hex2[i]
                if (h1 == h2) {
                    h1 = GREEN + h1
                    h2 = GREEN + h2
                } else {
                    h1 = RED + h1
                    h2 = RED + h2
                    isDif = true
                }
            } else {
                if (h1 == null) {
                    h1 = RED + hex1[i]
                }
                if (h2 == null) {
                    h2 = RED + hex2[i]
                }
            }

            numberLine.append(UNKNOWN_COLOR).append(getFixedNumber(i)).append(" ")
            hex1b.append(" ").append(h1).append(" ")
            hex2b.append(" ").append(h2).append(" ")
            if (isDif) {
                ++dif
            }

            //doConstReplacement(hex1b);
            //doConstReplacement(hex2b);
        }

        return builder.append(" ").append(dif).append(" 个不同").append("\n")
            .append(numberLine).append("\n")
            .append(hex1ConstName).append("\n")
            .append(hex1b).append("\n")
            .append(hex2b).append("\n")
            .append(hex2ConstName).append("\n")
            .toString()


    }

    fun String.colorize(ignoreUntilFirstConst: Boolean = false): String {
        val builder = StringBuilder()

        val hex = trim { it <= ' ' }.replace("\n", "").split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
        val constMatcher1 = ConstMatcher(this)

        val numberLine = StringBuilder()
        val hex1ConstName = StringBuilder()
        val hex1b = StringBuilder()

        buildConstNameChain(length, constMatcher1, hex1ConstName)

        var firstConst: String? = null
        var constNameOffset = 0//已经因为还没到第一个const跳过了几个char
        for (i in hex.indices) {
            var h1: String? = null

            val matchedConstName = constMatcher1.getMatchedConstName(i)
            if (matchedConstName != null) {
                firstConst = firstConst ?: matchedConstName
                h1 = BLUE + hex[i]
            }

            if (!ignoreUntilFirstConst || firstConst != null) {//有过任意一个 Const
                if (h1 == null) {
                    h1 = GREEN + hex[i]
                }
                numberLine.append(UNKNOWN_COLOR).append(getFixedNumber(i)).append(" ")
                hex1b.append(" ").append(h1).append(" ")
            } else {
                constNameOffset++
            }
        }

        return builder.append("\n")
            .append(numberLine).append("\n")
            .append(if (firstConst == null) hex1ConstName else {
                with(hex1ConstName) {
                    val index = indexOf(firstConst)
                    if (index == -1) toString() else " " + substring(index, length)
                }
            }).append("\n")
            .append(hex1b).append("\n")
            .toString()
    }


    private fun getFixedNumber(number: Int): String {
        if (number < 10) {
            return "00$number"
        }
        return if (number < 100) {
            "0$number"
        } else number.toString()
    }

}