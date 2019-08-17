package net.mamoe.mirai.network

import java.net.InetAddress
import java.util.*
import java.util.stream.Collectors

/**
 * @author Him188moe @ Mirai Project
 */
interface Protocol {
    companion object {
        val SERVER_IP: ArrayList<String> = object : ArrayList<String>() {
            init {
                add("183.60.56.29")

                arrayOf(
                        "sz2.tencent.com",
                        "sz3.tencent.com",
                        "sz4.tencent.com",
                        "sz5.tencent.com",
                        "sz6.tencent.com",
                        "sz8.tencent.com",
                        "sz9.tencent.com"
                ).forEach { this.add(InetAddress.getByName(it).hostAddress) }
            }
        }


        const val head = "02"
        const val ver = "37 13 "
        const val fixVer = "03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 "
        const val tail = " 03"
        const val _fixVer = "02 00 00 00 01 01 01 00 00 68 20 "
        const val _0825data0 = "00 18 00 16 00 01 "
        const val _0825data2 = "00 00 04 53 00 00 00 01 00 00 15 85 "
        const val _0825key = "A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D"
        const val redirectionKey = "A8 F2 14 5F 58 12 60 AF 07 63 97 D6 76 B2 1A 3B"
        const val publicKey = "02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3"
        const val shareKey = "1A E9 7F 7D C9 73 75 98 AC 02 E0 80 5F A9 C6 AF"
        const val _0836fix = "06 A9 12 97 B7 F8 76 25 AF AF D3 EA B4 C8 BC E7 "

        const val _00BaKey = "C1 9C B8 C8 7B 8C 81 BA 9E 9E 7A 89 E1 7A EC 94"
        const val _00BaFixKey = "69 20 D1 14 74 F5 B3 93 E4 D5 02 B3 71 1A CD 2A"

        const val encryptKey = "“BA 42 FF 01 CF B4 FF D2 12 F0 6E A7 1B 7C B3 08”"

        const val _0836_622_fix2 = "00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B";
        const val _0836_622_fix1 = "03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03 00 19";
        const val _0836key1 = "EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA"

        @ExperimentalUnsignedTypes
        fun hexToBytes(hex: String): ByteArray = hexToUBytes(hex).toByteArray()

        @ExperimentalUnsignedTypes
        fun hexToUBytes(hex: String): UByteArray = Arrays
                .stream(hex.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { value -> value.trim { it <= ' ' } }
                .map { s -> s.toUByte(16) }
                .collect(Collectors.toList()).toUByteArray()

    }
}
