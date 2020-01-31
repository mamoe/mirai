package net.mamoe.mirai.qqandroid.network.http

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.setCookie
import io.ktor.http.userAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.io.readRemaining
import kotlinx.coroutines.withContext
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.mirai.utils.io.readRemainingBytes
import net.mamoe.mirai.utils.io.toUHexString

/**
 * 好像不需要了
 */
object HttpRequest {
    private lateinit var cookie: String
}


internal suspend fun HttpClient.getPTLoginCookies(
    client: QQAndroidClient
): String {
    //$"https://ssl.ptlogin2.qq.com/jump?pt_clientver=5593&pt_src=1&keyindex=9&ptlang=2052&clientuin={QQ}&clientkey={Util.ToHex(TXProtocol.BufServiceTicketHttp, "", "{0}")}&u1=https:%2F%2Fuser.qzone.qq.com%2F417085811%3FADUIN=417085811%26ADSESSION={Util.GetTimeMillis(DateTime.Now)}%26ADTAG=CLIENT.QQ.5593_MyTip.0%26ADPUBNO=26841&source=namecardhoverstar"
    // "https://ssl.ptlogin2.qq.com/jump?pt_clientver=5509&pt_src=1&keyindex=9&clientuin={0}&clientkey={1}&u1=http%3A%2F%2Fqun.qq.com%2Fmember.html%23gid%3D168209441",
    val res = post<HttpResponse> {
        println(client.wLoginSigInfo.userStWebSig.data.toUHexString().replace(" ", "").toLowerCase())
        url {
            protocol = URLProtocol.HTTPS
            host = "ssl.ptlogin2.qq.com"
            path(
                "/jump?pt_clientver=5509&pt_src=1&keyindex=9&clientuin=${client.uin}&clientkey=${client.wLoginSigInfo.userStWebSig.data.toUHexString().replace(
                    " ",
                    ""
                )}&u1=http%3A%2F%2Fqun.qq.com%2Fmember.html%23gid%3D168209441&FADUIN=417085811&ADSESSION=${currentTimeMillis}&source=namecardhoverstar"
            )
        }
        headers {
            userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36")
        }
    }

    println(res.status)
    println(res.setCookie())
    println(res.content.readRemaining().readRemainingBytes().toUHexString())
    return "done";
}


internal suspend fun HttpClient.getGroupList(
    client: QQAndroidClient
): String {
    // "https://ssl.ptlogin2.qq.com/jump?pt_clientver=5509&pt_src=1&keyindex=9&clientuin={0}&clientkey={1}&u1=http%3A%2F%2Fqun.qq.com%2Fmember.html%23gid%3D168209441",
    val res = get<HttpResponse> {
        url {
            protocol = URLProtocol.HTTPS
            host = "ssl.ptlogin2.qq.com"
            path("jump")
            parameters["pt_clientver"] = "5509"
            parameters["pt_src"] = "1"
            parameters["keyindex"] = "9"
            parameters["u1"] = "http%3A%2F%2Fqun.qq.com%2Fmember.html%23gid%3D168209441"
            parameters["clientuin"] = client.uin.toString()
            parameters["clientkey"] = client.wLoginSigInfo.userStWebSig.data.toUHexString()
        }
        headers {
            userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36")
        }
    }

    println(res.status)
    println(res.setCookie())
    return "done";
}
