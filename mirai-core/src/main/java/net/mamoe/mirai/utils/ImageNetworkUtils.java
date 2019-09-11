package net.mamoe.mirai.utils;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageNetworkUtils {
    public static void postImage(String ukey, int fileSize, String g_uin,String groupCode, byte[] img){
        //http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc&ukey=” ＋ 删全部空 (ukey) ＋ “&filesize=” ＋ 到文本 (fileSize) ＋ “&range=0&uin=” ＋ g_uin ＋ “&groupcode=” ＋ Group
        StringBuilder builder = new StringBuilder("http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc");
        builder.append("&ukey=")
                .append(ukey.trim())
                .append("&filezise=").append(fileSize)
                .append("&range=").append("0")
                .append("&uin=").append(g_uin)
                .append("&groupcode=").append(groupCode);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(builder.toString()).openConnection();
            conn.setRequestProperty("User-agent","QQClient");
            conn.setRequestProperty("Content-length","" + fileSize);
            conn.setRequestMethod("POST");
            conn.getOutputStream().write(img);

            conn.connect();
            System.out.println(conn.getResponseCode());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
