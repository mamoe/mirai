package net.mamoe.mirai.utils;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author NaturalHG
 */
public class ImageNetworkUtils {
    public static boolean postImage(String uKeyHex, int fileSize, long botNumber, long groupCode, byte[] img) throws IOException {
        //http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc&ukey=” ＋ 删全部空 (ukey) ＋ “&filesize=” ＋ 到文本 (fileSize) ＋ “&range=0&uin=” ＋ g_uin ＋ “&groupcode=” ＋ Group

        String builder = "http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc" +
                "&ukey=" + uKeyHex.replace(" ", "") +
                "&filezise=" + fileSize +
                "&range=" + "0" +
                "&uin=" + botNumber +
                "&groupcode=" + groupCode;
        HttpURLConnection conn = (HttpURLConnection) new URL(builder).openConnection();
        conn.setRequestProperty("User-Agent", "QQClient");
        conn.setRequestProperty("Content-Length", "" + fileSize);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(img);

        conn.connect();
        return conn.getResponseCode() == 200;
    }
}
