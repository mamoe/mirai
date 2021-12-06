package io.github.mzdluo123.mirai.android.utils

import io.github.mzdluo123.mirai.android.BotApplication
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Dns
import java.net.InetAddress

@ExperimentalUnsignedTypes
class SafeDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return runBlocking {
            val res =
                BotApplication.httpClient.value.get<String>("https://cloudflare-dns.com/dns-query?name=$hostname&type=A") {
                    headers.append("accept", "application/dns-json")
                }
            val json = BotApplication.json.value.parseToJsonElement(res)
            return@runBlocking listOf(
                InetAddress.getByName(
                    json.jsonObject["Answer"]?.jsonArray?.get(
                        0
                    )?.jsonObject?.get("data")?.jsonPrimitive?.content
                )
            )
        }
    }
}
