package net.mamoe.mirai.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.InetAddress
import java.security.MessageDigest
import java.util.zip.CRC32

actual val deviceName: String = InetAddress.getLocalHost().hostName


actual fun crc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }

actual fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

actual fun solveIpAddress(hostname: String): String = InetAddress.getByName(hostname).hostAddress

actual fun localIpAddress(): String = InetAddress.getLocalHost().hostAddress

actual suspend fun httpPostGroupImage(
        uKeyHex: String,
        fileSize: Int,
        botNumber: Long,
        groupCode: Long,
        imageData: ByteArray
): Boolean = Jsoup
        .connect("http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc" +
                "&ukey=" + uKeyHex.replace(" ", "") +
                "&filezise=" + fileSize +
                "&range=" + "0" +
                "&uin=" + botNumber +
                "&groupcode=" + groupCode)
        .userAgent("QQClient")
        .header("Content-Length", fileSize.toString())
        .requestBody(String(imageData))
        .method(Connection.Method.POST)
        .ignoreContentType(true)
        .let {
            withContext(Dispatchers.IO) {
                it.execute()
            }
        }
        /*
        val conn = URL(builder).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "QQClient")
        conn.setRequestProperty("Content-Length", "" + fileSize)
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.outputStream.write(img)

        conn.connect()*/
        .statusCode() == 200