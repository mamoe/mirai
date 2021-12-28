package io.github.mzdluo123.mirai.android.utils

import io.github.mzdluo123.mirai.android.BotApplication
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalUnsignedTypes::class)

suspend fun paste(text: String): String {
    return withContext(Dispatchers.IO) {
        val res = BotApplication.httpClient.value.post<HttpResponse>("https://paste.ubuntu.com/") {
            body = MultiPartFormDataContent(formData {
                append("poster", "MiraiAndroid")
                append("syntax", "text")
                append("expiration", "")
                append("content", text)
            })
        }
        return@withContext "https://paste.ubuntu.com" + res.headers["Location"].toString()
    }
}