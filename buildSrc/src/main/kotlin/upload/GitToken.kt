package upload

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object GitToken {

    private fun getGitToken(): String {
        with(File(System.getProperty("user.dir")).parent + "/token.txt") {
            println("reading token file in $this")
            return File(this).readText()
        }
    }

    fun upload(file: File, url: String) {
        val begin = System.currentTimeMillis()
        println("上传开始...")
        //StringBuffer result = new StringBuffer();
        //StringBuffer result = new StringBuffer();
        var `in`: BufferedReader? = null
        var conn: HttpURLConnection? = null
        conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 120000
        conn.readTimeout = 120000
        // 设置
        conn.doOutput = true // 需要输出
        conn.doInput = true // 需要输入
        conn.useCaches = false // 不允许缓存
        conn.requestMethod = "PUT" // 设置PUT方式连接
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "token " + getGitToken())
        conn.setRequestProperty("User-Agent", "Github File Uploader App")
        conn.connect()
        // 传输数据
        val dos = DataOutputStream(conn.outputStream)
        // 传输json头部
        dos.writeBytes("{\"message\":\".\",\"content\":\"")
        // 传输文件内容
        val buffer = ByteArray(1024 * 1002) // 3的倍数
        val raf = RandomAccessFile(file, "r")
        var size: Long = raf.read(buffer).toLong()
        while (size > -1) {
            if (size == buffer.size.toLong()) {
                dos.write(Base64.getEncoder().encode(buffer))
            } else {
                val tmp = ByteArray(size.toInt())
                System.arraycopy(buffer, 0, tmp, 0, size.toInt())
                dos.write(Base64.getEncoder().encode(tmp))
            }
            size = raf.read(buffer).toLong()
        }
        raf.close()
        // 传输json尾部
        dos.writeBytes("\"}")
        dos.flush()
        dos.close()
        `in` = BufferedReader(InputStreamReader(conn.inputStream))
        var line: String?
        while (`in`.readLine().also { line = it } != null) {
            //result.append(line).append("\n");
        }
        val end = System.currentTimeMillis()
        System.out.printf("Upload finished within %d seconds\n", (end - begin) / 1000)
        //result.toString()
        //result.toString()
    }
}