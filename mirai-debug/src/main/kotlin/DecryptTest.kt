/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UNUSED_VARIABLE")

import net.mamoe.mirai.utils.io.encodeToString
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.toUHexString
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream


fun main() {
    val short: Short = 0x8b1f.toShort()
    //                                 7字节相同|                                                                                                                                                                                               |18字节向东
    val bytes =
        "AA 02 56 30 01 3A 40 53 4B 4B 2F 6F 59 33 42 39 2F 68 56 54 45 4B 65 6A 5A 39 35 45 4D 7A 68 5A 2F 6F 4A 42 79 35 36 61 6F 50 59 32 6E 51 49 77 41 67 37 47 51 33 34 65 72 43 4C 41 72 50 4B 56 39 35 43 76 65 34 64 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00"
            .hexToBytes()
    println(bytes.encodeToString())

    val string = bytes.encodeToString()

    // println("53 4B 4B 2F 6F 59 33 42 39 2F 68 56 54 45 4B 65 6A 5A 39 35 45 4D 7A 68 5A 2F 6F 4A 42 79 35 36 61 6F 50 59 32 6E 51 49 77 41 67 37 47 51 33 34 65 72 43 4C 41 72 50 4B 56 39 35 43 76 65 34 64".hexToBytes().encodeToString())
    println("53 4B 4B 2F 6F 59 33 42 39 2F 68 56 54 45 4B 65 6A 5A 39 35 45 4D 7A 68 5A 2F 6F 4A 42 79 35 36 61 6F 50 59 32 6E 51 49 77 41 67 37 47 51 33 34 65 72 43 4C 41 72 50 4B 56 39 35 43 76 65 34 64"
        .hexToBytes().unbase64().encodeToString()
    )
    println("53 4B 4B 2F 6F 59 33 42 39 2F 68 56 54 45 4B 65 6A 5A 39 35 45 4D 7A 68 5A 2F 6F 4A 42 79 35 36 61 6F 50 59 32 6E 51 49 77 41 67 37 47 51 33 34 65 72 43 4C 41 72 50 4B 56 39 35 43 76 65 34 64"
        .hexToBytes().unbase64().toUHexString())

    //base64解密结果 48 A2 BF A1 8D C1 F7 F8 55 4C 42 9E 8D 9F 79 10 CC E1 67 FA 09 07 2E 7A 6A 83 D8 DA 74 08 C0 08 3B 19 0D F8 7A B0 8B 02 B3 CA 57 DE 42 BD EE 1D


    println()
    println()


    println(Base64.getEncoder().encodeToString(".".repeat(1000).toByteArray()))

    //                     01  78
    val data2 =
        "9C  CD  92  BB  4E  C3  30  14  86  77  9E  C2  32  23  4A  73  69  D3  76  48  52  25  25  BD  48  84  56  54  15  97  05  85  C4  09  2E  B9  A8  4E  D2  92  6E  DD  10  0C  88  81  0D  84  90  60  40  42  C0  C4  D6  C7  69  E8  63  E0  94  22  46  46  F8  2D  59  F2  B1  7F  9F  E3  EF  58  AA  9D  FA  1E  18  21  12  E1  30  90  21  5F  E0  20  40  81  15  DA  38  70  65  98  C4  0E  53  85  35  65  0D  50  49  7E  E4  82  23  82  91  23  C3  C2  3F  17  04  FE  A1  83  3D  B4  6D  FA  48  86  42  45  2F  F3  82  58  64  CA  A2  2E  32  25  AD  51  66  54  AD  21  32  AA  AA  AB  1C  5F  15  04  AE  5E  F9  76  F4  F0  84  3A  28  05  D3  8A  97  48  46  18  8D  8D  C4  8B  B1  11  B9  10  38  9E  49  B9  14  21  88  10  19  61  0B  B5  37  E9  4A  CC  CD  04  45  D8  96  E1  38  ED  B7  BA  9D  81  53  2F  A5  7B  A1  A8  B1  29  6E  69  EE  C4  68  75  DB  FE  46  C7  76  3B  27  BA  67  F4  93  DD  83  DE  7E  33  16  06  CD  B2  C6  0F  35  42  D8  52  63  C7  DE  1A  0E  86  1A  04  0A  90  70  8C  7C  E0  99  69  98  C4  B4  27  90  46  62  1C  7B  48  01  7F  CD  F5  37  01  89  5D  55  0A  A4  63  A2  48  2C  9D  56  C5  03  2B  F4  42  22  C3  F5  2A  97  0F  FA  A8  EC  EE  F1  E3  E6  82  CF  6E  EF  17  B3  E7  F9  E5  55  F6  7E  96  4D  5F  C1  CF  1D  12  9B  83  50  A4  28  4C  88  85  40  B0  6C  E6  62  7A  3E  7F  78  5A  BC  BC  CD  67  D7  90  66  F8  DA  CC  0F  D3  FF  A9  7C  02  73  51  C1  65".replace(
            "  ",
            " "
        )
            .hexToBytes()
    println(data2.size)
    println(data2.unbase64().toUHexString())

}

fun ByteArray.unbase64() = Base64.getDecoder().decode(this)

fun ByteArray.ungzip(): ByteArray {
    val out = ByteArrayOutputStream()
    val `in` = ByteArrayInputStream(this)
    val ungzip = GZIPInputStream(`in`)
    val buffer = ByteArray(256)
    var n: Int
    while (ungzip.read(buffer).also { n = it } >= 0) {
        out.write(buffer, 0, n)
    }

    return out.toByteArray()
}