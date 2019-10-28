@file:Suppress("ObjectPropertyName", "unused", "NonAsciiCharacters", "MayBeConstant")

import net.mamoe.mirai.utils.io.printCompareHex


fun main() {
//    println(HexComparator.printColorize("00 00 00 25 00 08 00 02 00 01 00 09 00 06 00 01 00 00 00 01 00 0A 00 04 01 00 00 00 00 01 00 04 00 00 00 00 00 03 00 01 01 38 03 3E 03 3F A2 76 E4 B8 DD E7 86 74 F2 64 55 AD 9A EB 2F B9 DF F1 7F 8C 28 00 0B 78 14 5D A2 F5 CB 01 1D 00 00 00 00 01 00 00 00 01 4D 53 47 00 00 00 00 00 5D A2 F5 CA 9D 26 CB 5E 00 00 00 00 0C 00 86 22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 00 00 01 00 09 01 00 06 E4 BD A0 E5 A5 BD 0E 00 07 01 00 04 00 00 00 09 19 00 18 01 00 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00"))
    while (true) {
        println("Hex1: ")
        val hex1 = readLine()!!
        println("Hex2: ")
        val hex2 = readLine()!!
        println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        println(printCompareHex(hex1, hex2))
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