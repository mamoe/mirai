package net.mamoe.mirai.utils;

import org.apache.commons.httpclient.util.HttpURLConnection;

import java.io.IOException;
import java.net.URL;

/**
 * @author NaturalHG
 */
public class ImageNetworkUtils {
    public static void postImage(String uKeyHex, int fileSize, String qqNumber, String groupCode, byte[] img) throws IOException {
        //http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc&ukey=” ＋ 删全部空 (ukey) ＋ “&filesize=” ＋ 到文本 (fileSize) ＋ “&range=0&uin=” ＋ g_uin ＋ “&groupcode=” ＋ Group
        StringBuilder builder = new StringBuilder("http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc");
        builder.append("&ukey=")
                .append(uKeyHex.trim())
                .append("&filezise=").append(fileSize)
                .append("&range=").append("0")
                .append("&uin=").append(qqNumber)
                .append("&groupcode=").append(groupCode);

        HttpURLConnection conn = (HttpURLConnection) new URL(builder.toString()).openConnection();
        conn.setRequestProperty("User-agent", "QQClient");
        conn.setRequestProperty("Content-length", "" + fileSize);
        conn.setRequestMethod("POST");
        conn.getOutputStream().write(img);

        conn.connect();
        System.out.println(conn.getResponseCode());
        System.out.println(conn.getResponseMessage());
    }
}
