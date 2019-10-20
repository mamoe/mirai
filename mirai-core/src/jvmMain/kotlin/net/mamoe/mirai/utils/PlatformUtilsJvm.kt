@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import org.jsoup.Connection
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.security.MessageDigest
import java.util.zip.CRC32

actual val deviceName: String = InetAddress.getLocalHost().hostName


actual fun crc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }

actual fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

actual fun solveIpAddress(hostname: String): String = InetAddress.getByName(hostname).hostAddress

actual fun localIpAddress(): String = InetAddress.getLocalHost().hostAddress

fun main() {
    "00 00 00 08 00 00 01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 F1 C0 A1 BF 05 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03 4A 80 01 B5 29 1A 1B 0E 63 79 8B 34 B1 4E 2A 2A 9E 69 09 A7 69 F5 C6 4F 95 DA 96 A9 1B E3 CD 6F 3D 30 EE 59 C0 30 22 BF F0 2D 88 2D A7 6C B2 09 AD D6 CE E1 46 84 FC 7D 19 AF 1A 37 91 98 AD 2C 45 25 AA 17 2F 81 DC 5A 7F 30 F4 2D 73 E5 1C 8B 8A 23 85 42 9D 8D 5C 18 15 32 D1 CA A3 4D 01 7C 59 11 73 DA B6 09 C2 6D 58 35 EF 48 88 44 0F 2D 17 09 52 DF D4 EA A7 85 2F 27 CE DF A8 F5 9B CD C9 84 C2 52 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 5A 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 60 00 68 80 80 08 20 01"
            .printStringFromHex()
    println(md5("00 00 00 08 00 00 01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 F1 C0 A1 BF 05 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03 4A 80 01 B5 29 1A 1B 0E 63 79 8B 34 B1 4E 2A 2A 9E 69 09 A7 69 F5 C6 4F 95 DA 96 A9 1B E3 CD 6F 3D 30 EE 59 C0 30 22 BF F0 2D 88 2D A7 6C B2 09 AD D6 CE E1 46 84 FC 7D 19 AF 1A 37 91 98 AD 2C 45 25 AA 17 2F 81 DC 5A 7F 30 F4 2D 73 E5 1C 8B 8A 23 85 42 9D 8D 5C 18 15 32 D1 CA A3 4D 01 7C 59 11 73 DA B6 09 C2 6D 58 35 EF 48 88 44 0F 2D 17 09 52 DF D4 EA A7 85 2F 27 CE DF A8 F5 9B CD C9 84 C2 52 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 5A 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 60 00 68 80 80 08 20 01").toUHexString())
}


actual suspend fun httpPostFriendImage(
        uKeyHex: String,
        fileSize: Long,
        botNumber: UInt,
        qq: UInt,
        imageData: ByteReadPacket
): Boolean {/*Jsoup
        //htdata2.qq.com

        //	101.227.143.109/cgi-bin/httpconn
        //	?htcmd=0x6ff0070
        //	&ver=5603
        //	&ukey=3B121C959B85035F12497519221FB9E09740E477D3A440D28253E96C95BD72EA1D11B25189894F0256F3E0F3D553FB92925A8834F85583C78D0D9639A3F35C730783D45C065FF9E9E74765183A11492D50750C6BB5DCAD9F171285B68F6A11061CDDA740AD2DCD28D5B2DB2D6440143FA53F1B6F14584DB49E926FDDC4F49907
        //	&filesize=137791
        //	&range=0
        //	&uin=1040400290
        .connect("http://101.227.143.109/cgi-bin/httpconn" +
                "?htcmd=0x6ff0070" +
                "&ver=5603" +
                "&ukey=" + uKeyHex.replace(" ", "") +
                "&filezise=" + fileSize +
                "&range=" + "0" +
                "&uin=" + botNumber.toLong())
        //.userAgent("QQClient")
        .header("Content-Length", fileSize.toString())
        .requestBody(String(imageData, Charset.forName("ASCII")))
        .method(Connection.Method.POST)
        .ignoreContentType(true)
        .let {
            withContext(Dispatchers.IO) {
                it.execute()
            }
        };*/

    val conn = URL("http://htdata2.qq.com/cgi-bin/httpconn" +
            "?htcmd=0x6ff0070" +
            "&ver=5603" +
            "&ukey=" + uKeyHex.replace(" ", "") +
            "&filezise=" + fileSize +
            "&range=" + "0" +
            "&uin=" + botNumber.toLong()).openConnection() as HttpURLConnection
    conn.setRequestProperty("User-Agent", "QQClient")
    conn.setRequestProperty("Content-Length", "" + fileSize)
    conn.setRequestProperty("Connection", "Keep-Alive")
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.doInput = true
    withContext(Dispatchers.IO) {
        conn.connect()
    }

    conn.outputStream.writePacket(imageData)

    println(conn.responseMessage)
    println(conn.responseCode)
    return conn.responseCode == 200
}

/**
 * 上传群图片
 */
actual suspend fun httpPostGroupImage(uKeyHex: String, fileSize: Long, imageData: ByteReadPacket): Boolean {
    val conn = URL("http://htdata2.qq.com/cgi-bin/httpconn" +
            "?htcmd=0x6ff0071" +
            "&term=pc" +
            "&ver=5603" +
            "&ukey=" + uKeyHex.replace(" ", "")).openConnection() as HttpURLConnection
    conn.setRequestProperty("Content-Length", fileSize.toString())
    conn.setRequestProperty("Connection", "Keep-Alive")
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.doInput = true
    withContext(Dispatchers.IO) {
        conn.connect()
    }

    val stream = conn.outputStream
    stream.writePacket(imageData)

    println(conn.responseMessage)
    println(conn.responseCode)
    return conn.responseCode == 200
}

private suspend fun Connection.suspendExecute(): Connection.Response = withContext(Dispatchers.IO) {
    execute()
}

private fun OutputStream.writePacket(packet: ByteReadPacket) {
    val byteArray = ByteArray(1)
    repeat(packet.remaining.toInt()) {
        packet.readAvailable(byteArray)
        this.write(byteArray)
    }
}