@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network

import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.solveIpAddress

object TIMProtocol {
    val SERVER_IP: List<String> = {
        //add("183.60.56.29")
        arrayOf(
            "sz3.tencent.com",
            "sz4.tencent.com",
            "sz5.tencent.com",
            "sz6.tencent.com",
            "sz8.tencent.com",
            "sz9.tencent.com",
            "sz2.tencent.com"
        ).map { solveIpAddress(it) } // 需 IPv4 地址
    }()//不使用lazy, 在初始化时就加载.

    val head = "02".hexToBytes()
    val ver = "37 13".hexToBytes()// TIM 最新版中这个有时候是 38 03
    val fixVer = "03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00".hexToBytes()
    val tail = "03".hexToBytes()
    /**
     * _fixVer
     */
    val fixVer2 = "02 00 00 00 01 01 01 00 00 68 20".hexToBytes()
    //                   02 38 03 00 CD 48 68 3E 03 3F A2 02 00 00 00
    val version0x02 = "02 00 00 00 01 2E 01 00 00 69 35".hexToBytes()
    val version0x04 = "04 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 00 00 00".hexToBytes()

    val constantData1 = "00 18 00 16 00 01 ".hexToBytes()
    val constantData2 = "00 00 04 53 00 00 00 01 00 00 15 85 ".hexToBytes()


    //todo 使用 byte array

    /**
     * Touch 发出时写入, 并用于加密, 接受 sendTouch response 时解密.
     */
    val touchKey = "A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D".hexToBytes()//16


    //统一替换为了 touchKey
    ///**
    // * Redirection 发出时写入, 并用于加密, 接受 Redirection response 时解密.
    // * 这个 key 似乎是可以任意的.
    // */
    //val redirectionKey = "A8 F2 14 5F 58 12 60 AF 07 63 97 D6 76 B2 1A 3B"//16

    /**
     * 并非常量. 设置为常量是为了让 [shareKey] 为常量
     */
    val publicKey = "02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3".hexToBytes()//25

    /**
     * 并非常量. 设置为常量是为了让 [shareKey] 为常量
     *
     * LoginResend 和 PasswordSubmission 时写入, 但随后都使用 shareKey 加密, 收到回复也是用的 share key
     */
    val key0836 = "EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA".hexToBytes()//16

    /**
     * 并非常量. 是 publicKey 与 key0836 的算法计算结果
     */
    //val shareKey = "5B 6C 91 55 D9 92 F5 A7 99 85 37 76 3D 0F 08 B7"//16
    val shareKey = "1A E9 7F 7D C9 73 75 98 AC 02 E0 80 5F A9 C6 AF".hexToBytes()//16//original

    val key00BA = "C1 9C B8 C8 7B 8C 81 BA 9E 9E 7A 89 E1 7A EC 94".hexToBytes()
    val key00BAFix = "69 20 D1 14 74 F5 B3 93 E4 D5 02 B3 71 1A CD 2A".hexToBytes()

    /**
     * 0836_622_fix2
     */
    val passwordSubmissionTLV2 =
        "00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B".hexToBytes()
    /**
     * 0836_622_fix1
     */
    val passwordSubmissionTLV1 = "03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03".hexToBytes()//19
    //               最新版              03 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 02 01 03
    //               第一版 1.0.2        03 00 00 00 01 2E 01 00 00 68 13 00 00 00 00 00 02 01 03
    //                  1.0.4           03 00 00 00 01 2E 01 00 00 68 27 00 00 00 00 00 02 01 03
    //                1.1               03 00 00 00 01 2E 01 00 00 68 3F 00 00 00 00 00 02 01 03
    //                 1.2              03 00 00 00 01 2E 01 00 00 68 44 00 00 00 00 00 02 01 03

    /**
     * 发送/接受消息中的一个const (?)
     * length=15
     */
    val messageConst1 = "00 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91".hexToBytes()
    val messageConstNewest = "22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91".hexToBytes()
    //               TIM最新   22 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91
}
