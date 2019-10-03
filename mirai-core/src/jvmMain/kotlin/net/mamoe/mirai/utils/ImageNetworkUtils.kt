package net.mamoe.mirai.utils

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author NaturalHG
 */
object ImageNetworkUtils {
    @Throws(IOException::class)
    fun postImage(uKeyHex: String, fileSize: Int, botNumber: Long, groupCode: Long, img: ByteArray): Boolean {
        //http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc&ukey=” ＋ 删全部空 (ukey) ＋ “&filesize=” ＋ 到文本 (fileSize) ＋ “&range=0&uin=” ＋ g_uin ＋ “&groupcode=” ＋ Group

        val builder = "http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc" +
                "&ukey=" + uKeyHex.replace(" ", "") +
                "&filezise=" + fileSize +
                "&range=" + "0" +
                "&uin=" + botNumber +
                "&groupcode=" + groupCode
        val conn = URL(builder).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "QQClient")
        conn.setRequestProperty("Content-Length", "" + fileSize)
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.outputStream.write(img)

        conn.connect()
        return conn.responseCode == 200
    }
}
