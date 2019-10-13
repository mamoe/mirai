@file:Suppress("ObjectPropertyName", "unused", "NonAsciiCharacters", "MayBeConstant")

import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.toUHexString
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.lang.reflect.Field
import java.util.*
import kotlin.math.max

/**
 * Hex 比较器, 并着色已知常量
 *
 * This could be used to check packet encoding..
 * but better to run under UNIX
 *
 * @author NaturalHG
 * @author Him188moe
 */
object HexComparator {

    private val RED = "\u001b[31m"
    private val GREEN = "\u001b[33m"
    private val UNKNOWN = "\u001b[30m"
    private val BLUE = "\u001b[34m"

    private val clipboardString: String?
        get() {
            val trans = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    return trans.getTransferData(DataFlavor.stringFlavor) as String
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            return null
        }

    class ConstMatcher constructor(hex: String) {
        private val matches = LinkedList<Match>()

        object TestConsts {
            val NIU_BI = "牛逼".toByteArray().toUHexString()
            val _1994701021 = 1994701021.toUHexString(" ")
            val _1040400290 = 1040400290.toUHexString(" ")
            val _580266363 = 580266363.toUHexString(" ")

            val _1040400290_ = "3E 03 3F A2"
            val _1994701021_ = "76 E4 B8 DD"
            val _jiahua_ = "B1 89 BE 09"
            val _Him188moe_ = "Him188moe".toByteArray().toUHexString()
            val 发图片 = "发图片".toByteArray().toUHexString()
            val 群 = "群".toByteArray().toUHexString()
            val 你好 = "你好".toByteArray().toUHexString()

            val SINGLE_PLAIN_MESSAGE_HEAD = "00 00 01 00 09 01"

            val MESSAGE_TAIL_10404 = "0E  00  07  01  00  04  00  00  00  09 19  00  18  01  00  15  AA  02  12  9A  01  0F  80  01  01  C8  01  00  F0  01  00  F8  01  00  90  02  00"
                    .replace("  ", " ")
        }

        @Suppress("SpellCheckingInspection")
        object PacketIds {
            val heartbeat = "00 58"
            val friendmsgsend = "00 CD"
            val friendmsgevent = "00 CE"
        }

        init {
            CONST_FIELDS.forEach { field ->
                for (match in match(hex, field)) {
                    matches.add(Match(match, field.name))
                }
            }
        }

        fun getMatchedConstName(hexNumber: Int): String? {
            for (match in this.matches) {
                if (match.range.contains(hexNumber)) {
                    return match.constName
                }
            }
            return null
        }

        private class Match internal constructor(val range: IntRange, val constName: String)

        companion object {
            private val CONST_FIELDS: List<Field> = listOf(
                    TestConsts::class.java,
                    TIMProtocol::class.java,
                    PacketIds::class.java
            ).map { it.declaredFields }.flatMap { fields ->
                fields.map { field ->
                    field.trySetAccessible()
                    field
                }
            }
        }

        private fun match(hex: String, field: Field): List<IntRange> {
            val constValue: String
            try {
                constValue = (field.get(null) as String).trim { it <= ' ' }
                if (constValue.length / 3 <= 3) {//Minimum numbers of const hex bytes
                    return LinkedList()
                }
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (ignored: ClassCastException) {
                return LinkedList()
            }

            return object : LinkedList<IntRange>() {
                init {
                    var index = -1
                    index = hex.indexOf(constValue, index + 1)
                    while (index != -1) {
                        add(IntRange(index / 3, (index + constValue.length) / 3))

                        index = hex.indexOf(constValue, index + 1)
                    }
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

            numberLine.append(UNKNOWN).append(getFixedNumber(i)).append(" ")
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

    fun colorize(hex1s: String): String {
        val builder = StringBuilder()

        val hex1 = hex1s.trim { it <= ' ' }.replace("\n", "").split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
        val constMatcher1 = ConstMatcher(hex1s)

        val numberLine = StringBuilder()
        val hex1ConstName = StringBuilder()
        val hex1b = StringBuilder()

        buildConstNameChain(hex1s.length, constMatcher1, hex1ConstName)

        for (i in hex1.indices) {
            var h1: String? = null

            val matchedConstName = constMatcher1.getMatchedConstName(i)
            if (matchedConstName != null) {
                h1 = BLUE + hex1[i]
            }

            if (h1 == null) {
                h1 = hex1[i]
                h1 = GREEN + h1
            }
            numberLine.append(UNKNOWN).append(getFixedNumber(i)).append(" ")
            hex1b.append(" ").append(h1).append(" ")
        }

        return builder.append("\n")
                .append(numberLine).append("\n")
                .append(hex1ConstName).append("\n")
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

fun main() {
//    println(HexComparator.colorize("00 00 00 25 00 08 00 02 00 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 76 E4 B8 DD E7 86 74 F2 64 55 AD 9A EB 2F B9 DF F1 7F 8C 28 00 0B 78 14 5D A2 F5 CB 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D A2 F5 CA 9D 26 CB 5E 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E4 BD A0 E5 A5 BD 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00"))



    val scanner = Scanner(System.`in`)
    while (true) {
        println("Hex1: ")
        val hex1 = scanner.nextLine()
        println("Hex2: ")
        val hex2 = scanner.nextLine()
        println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        println(HexComparator.compare(hex1, hex2))
        println()
    }
    /*
    System.out.println(HexComparator.compare(
            //mirai

            "2A 22 96 29 7B 00 40 00 01 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 EC 21 40 06 18 89 54 BC Protocol.messageConst1 00 00 01 00 0A 01 00 07 E7 89 9B E9 80 BC 21\n"
            ,
            //e
            "2A 22 96 29 7B 00 3F 00 01 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 6B 8E 1A FE 39 0B FC Protocol.messageConst1 00 00 01 00 0A 01 00 07 6D 65 73 73 61 67 65"
    ));
     */


    /*
    System.out.println(HexComparator.compare(
            //e
            "90 5E 39 DF 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 7B 7B 7B 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC",
            //mirai
            "6F 0B DF 92 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 E9 E9 E9 E9 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC\n\n\n"
    ));*/
}