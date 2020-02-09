/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization

import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.utils.io.hexToBytes

class TestRequesetPacket {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            val data =
                "10 03 2C 3C 4C 56 0B 50 75 73 68 53 65 72 76 69 63 65 66 0E 53 76 63 52 65 71 52 65 67 69 73 74 65 72 7D 00 01 D6 00 08 00 01 06 0E 53 76 63 52 65 71 52 65 67 69 73 74 65 72 1D 00 01 BE 00 0A 02 DD B8 E4 76 10 07 2C 36 00 40 0B 5C 6C 7C 8C 9C AC B0 1D C0 01 D6 00 EC FD 10 00 00 10 07 EA 76 78 FD EB 06 C3 33 B2 18 80 32 15 09 BA F1 11 04 08 FC 12 F6 13 19 41 6E 64 72 6F 69 64 20 53 44 4B 20 62 75 69 6C 74 20 66 6F 72 20 78 38 36 F6 14 19 41 6E 64 72 6F 69 64 20 53 44 4B 20 62 75 69 6C 74 20 66 6F 72 20 78 38 36 F6 15 02 31 30 F0 16 01 F1 17 0F 06 FC 18 FC 1A F3 1B A9 00 FE 00 66 00 82 00 FC 1D F6 1E 04 4D 49 55 49 F6 1F 14 3F 4F 4E 45 50 4C 55 53 20 41 35 30 30 30 5F 32 33 5F 31 37 FD 21 00 00 0D 0A 04 08 2E 10 00 0A 05 08 9B 02 10 00 FC 22 FC 24 0B 8C 98 0C A8 0C".hexToBytes()

            println(data.loadAs(RequestPacket.serializer(), JceCharset.UTF8))
        }
    }
}