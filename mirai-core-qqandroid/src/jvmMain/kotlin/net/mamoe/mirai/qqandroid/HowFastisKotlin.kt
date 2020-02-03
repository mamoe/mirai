package net.mamoe.mirai.qqandroid

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.URLProtocol
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.io.readRemaining
import kotlinx.coroutines.launch
import kotlinx.io.core.readBytes


suspend fun main(){
    val lst = listOf<String>("N","n","M","m","S","s","L","l","1","2","3","4","5","0")
    fun rdm(l:Int):String{
        var s = "Pp";
        repeat(l){
            s+=lst.random()
        }
        return s
    }
    val lst2 = listOf<String>("1","2","3","4","5","6","7")
    fun rdmQQ(){
        var s = "1"
        repeat(8){
            s+=lst2.random()
        }
    }
    coroutineScope {
        repeat(1000) {
            launch {
                val r = HttpClient().get<HttpResponse>() {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "papl.lfdevs.com"
                        path("/check/regcheck")
                        parameters["c"] = "reg"
                        parameters["username"] = rdm(12)
                        parameters["email"] = rdm(5) + "@126.com"
                        parameters["pwd"] = rdm(10)
                        parameters["qq"] = rdmQQ().toString()
                        parameters["sex"] = "1"
                    }
                    headers {
                        header("referer","https://papl.lfdevs.com/index/login")
                        header("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                    }
                }
                if(r.status.value==200) {
                    println(r.status.toString() + "|" + String(r.content.readRemaining().readBytes()))
                }
            }
        }
    }
}

