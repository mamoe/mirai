import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class GitUploader {
    public static boolean create(String url, File file) {
        long begin = System.currentTimeMillis();
        System.out.println("上传开始...");
        //StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        HttpURLConnection conn = null;
        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(120000);
            conn.setReadTimeout(120000);
            // 设置
            conn.setDoOutput(true); // 需要输出
            conn.setDoInput(true); // 需要输入
            conn.setUseCaches(false); // 不允许缓存
            conn.setRequestMethod("PUT"); // 设置PUT方式连接

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "token " + getGitToken());
            conn.setRequestProperty("User-Agent", "Github File Uploader App");
            conn.connect();
            // 传输数据
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            // 传输json头部
            dos.writeBytes("{\"message\":\".\",\"content\":\"");
            // 传输文件内容
            byte[] buffer = new byte[1024 * 1002]; // 3的倍数
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long size = raf.read(buffer);
            while (size > -1) {
                if (size == buffer.length) {
                    dos.write(Base64.getEncoder().encode(buffer));
                } else {
                    byte tmp[] = new byte[(int) size];
                    System.arraycopy(buffer, 0, tmp, 0, (int) size);
                    dos.write(Base64.getEncoder().encode(tmp));
                }
                size = raf.read(buffer);
            }
            raf.close();
            // 传输json尾部
            dos.writeBytes("\"}");
            dos.flush();
            dos.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                //result.append(line).append("\n");
            }
        } catch (Exception e) {
            System.out.println("发送PUT请求出现异常！");
            e.printStackTrace();
            return false;
        } finally {
            try {
                in.close();
            } catch (Exception e2) {
            }
        }
        long end = System.currentTimeMillis();
        System.out.printf("上传结束，耗时 %ds\n", (end - begin) / 1000);
        //result.toString()
        return true;
    }

}
