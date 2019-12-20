package net.mamoe.mirai.data

import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.utils.Http

interface ImageLink {
    /**
     * 原图
     */
    val original: String

    suspend fun downloadAsByteArray(): ByteArray = download().readBytes()

    @UseExperimental(KtorExperimentalAPI::class)
    suspend fun download(): ByteReadPacket = Http.get(original)
}