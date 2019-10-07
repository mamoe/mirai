@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim

import net.mamoe.mirai.utils.TEA
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toUHexString
import java.net.InetAddress
import java.util.*
import java.util.stream.Collectors


/**
 * @author Him188moe
 */
object TIMProtocol {
    val SERVER_IP: List<String> by lazy {
        //add("183.60.56.29")
        val list = mutableListOf<String>()
        arrayOf(
                "sz3.tencent.com",
                "sz4.tencent.com",
                "sz5.tencent.com",
                "sz6.tencent.com",
                "sz8.tencent.com",
                "sz9.tencent.com",
                "sz2.tencent.com"
        ).forEach { list.add(InetAddress.getByName(it).hostAddress) }

        list.toList()
    }

    const val head = "02"
    const val ver = "37 13"
    const val fixVer = "03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00"
    const val tail = "03"
    /**
     * _fixVer
     */
    const val fixVer2 = "02 00 00 00 01 01 01 00 00 68 20"
    //                   02 38 03 00 CD 48 68 3E 03 3F A2 02 00 00 00
    /**
     * 0825data1
     */
    const val constantData1 = "00 18 00 16 00 01 "
    /**
     * 0825data2
     */
    const val constantData2 = "00 00 04 53 00 00 00 01 00 00 15 85 "

    /**
     * 0825 key
     *
     * Touch 发出时写入, 并用于加密, 接受 touch response 时解密.
     */
    const val touchKey = "A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D"//16

    /**
     * Redirection 发出时写入, 并用于加密, 接受 Redirection response 时解密.
     */
    const val redirectionKey = "A8 F2 14 5F 58 12 60 AF 07 63 97 D6 76 B2 1A 3B"//16

    /**
     *
     */
    const val publicKey = "02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3"//25

    /**
     * fix_0836_1
     *
     * LoginResend 和 PasswordSubmission 时写入, 但随后都使用 shareKey 加密, 收到回复也是用的 share key
     */
    const val key0836 = "EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA"//16

    @JvmStatic
    fun main(args: Array<String>) {
        println(TEA.decrypt(publicKey.hexToBytes(), key0836).toUHexString())
    }

    /**
     * 没有任何地方写入了这个 key
     */
    //const val shareKey = "5B 6C 91 55 D9 92 F5 A7 99 85 37 76 3D 0F 08 B7"//16
    const val shareKey = "1A E9 7F 7D C9 73 75 98 AC 02 E0 80 5F A9 C6 AF"//16//original

    const val key00BA = "C1 9C B8 C8 7B 8C 81 BA 9E 9E 7A 89 E1 7A EC 94"
    const val key00BAFix = "69 20 D1 14 74 F5 B3 93 E4 D5 02 B3 71 1A CD 2A"

    /**
     * 0836_622_fix2
     */
    const val passwordSubmissionTLV2 = "00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B"
    /**
     * 0836_622_fix1
     */
    const val passwordSubmissionTLV1 = "03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03"//19
    //               最新版              03 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 02 01 03
    //               第一版 1.0.2        03 00 00 00 01 2E 01 00 00 68 13 00 00 00 00 00 02 01 03
    //                  1.0.4           03 00 00 00 01 2E 01 00 00 68 27 00 00 00 00 00 02 01 03
    //                1.1               03 00 00 00 01 2E 01 00 00 68 3F 00 00 00 00 00 02 01 03
    //                 1.2              03 00 00 00 01 2E 01 00 00 68 44 00 00 00 00 00 02 01 03

    /**
     * 发送/接受消息中的一个const (?)
     * length=15
     */
    const val messageConst1 = "00 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91"

    private val hexToByteArrayCacheMap: MutableMap<Int, ByteArray> = mutableMapOf()


    fun hexToBytes(hex: String): ByteArray {
        hex.hashCode().let { id ->
            if (hexToByteArrayCacheMap.containsKey(id)) {
                return hexToByteArrayCacheMap[id]!!.clone()
            } else {
                hexToUBytes(hex).toByteArray().let {
                    hexToByteArrayCacheMap[id] = it.clone()
                    return it
                }
            }
        }
    }


    fun hexToUBytes(hex: String): UByteArray = Arrays
            .stream(hex.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .map { value -> value.trim { it <= ' ' } }
            .map { s -> s.toUByte(16) }
            .collect(Collectors.toList()).toUByteArray()
}
