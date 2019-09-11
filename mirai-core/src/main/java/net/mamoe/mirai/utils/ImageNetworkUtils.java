package net.mamoe.mirai.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class ImageNetworkUtils {
    public static void postImage(String ukey, int fileSize, String g_uin,String groupCode, String img){
        //http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc&ukey=” ＋ 删全部空 (ukey) ＋ “&filesize=” ＋ 到文本 (fileSize) ＋ “&range=0&uin=” ＋ g_uin ＋ “&groupcode=” ＋ Group
        StringBuilder builder = new StringBuilder("http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver=5515&term=pc");
        builder.append("&ukey=")
                .append(ukey.trim())
                .append("&filezise=").append(fileSize)
                .append("&range=").append("0")
                .append("&uin=").append(g_uin)
                .append("&groupcode=").append(groupCode);

        try {
        Connection.Response response = Jsoup.connect(builder.toString())
                    .ignoreContentType(true)
                    .requestBody(img)
                    .followRedirects(true)
                    .userAgent("QQClient")
                    .header("Content-Length","" + fileSize)
                    .execute();

        System.out.println(response.statusCode());
        System.out.println(response.statusMessage());
        System.out.println(response.body());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
