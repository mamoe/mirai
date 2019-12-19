package test

import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.readTLVMap
import net.mamoe.mirai.utils.io.toUHexString


@ExperimentalStdlibApi
@Suppress("EXPERIMENTAL_API_USAGE")
fun main() {
    val newMap =
        "4E 22 00 03 E5 AE 89 4E 25 00 06 35 31 31 34 39 35 4E 26 00 01 2D 4E 27 00 01 2D 4E 29 00 01 02 4E 2A 00 06 56 69 76 69 61 6E 4E 2B 00 10 31 35 36 31 34 38 39 31 33 40 71 71 2E 63 6F 6D 4E 2D 00 1D 68 74 74 70 3A 2F 2F 31 35 36 31 34 38 39 31 33 2E 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 4E 2E 00 02 33 00 4E 2F 00 04 33 33 39 00 4E 30 00 01 2D 4E 31 00 01 00 4E 33 00 2D E6 88 91 E7 95 99 E9 95 BF E7 9A 84 E5 A4 B4 E5 8F 91 EF BC 8C E6 98 AF E4 BD A0 E9 94 99 E8 BF 87 E7 9A 84 E5 B9 B4 E5 8D 8E 2E 2E 2E 4E 35 00 18 E5 B9 BF E4 B8 9C E6 8A 80 E6 9C AF E5 B8 88 E8 8C 83 E5 AD A6 E9 99 A2 4E 36 00 01 0A 4E 37 00 01 03 4E 38 00 01 01 4E 3F 00 04 07 C2 0B 02 4E 40 00 0C 00 00 00 31 00 00 34 34 00 00 00 33 4E 41 00 02 00 00 4E 42 00 02 00 00 4E 43 00 02 00 00 4E 45 00 01 21 4E 49 00 04 00 00 00 00 4E 4B 00 04 00 00 00 00 4E 4F 00 01 00 4E 54 00 00 4E 5B 00 00 52 0B 00 04 13 88 02 02 52 0F 00 14 00 00 00 00 00 00 00 00 12 05 10 58 89 10 00 00 00 00 00 00 5D C2 00 0C 00 00 00 31 00 00 34 34 00 00 31 34 5D C8 00 1E E7 B4 A2 E5 B0 BC EF BC 88 E4 B8 AD E5 9B BD EF BC 89 E6 9C 89 E9 99 90 E5 85 AC E5 8F B8 65 97 00 01 11 69 9D 00 04 00 00 00 00 69 A9 00 00 9D A5 00 02 00 00 A4 91 00 02 00 00 A4 93 00 02 00 00 A4 94 00 02 00 00 A4 9C 00 02 00 00 A4 B5 00 02 00 00"
            .hexToBytes().read {
                readTLVMap(tagSize = 2, expectingEOF = true)
            }
    newMap.forEach { (key, value) ->
        if (!(value.isEmpty() || value.all { it.toInt() == 0 })) {
            println(key.toUShort().toUHexString() + "=" + value.decodeToString())
        }
    }
    return
}