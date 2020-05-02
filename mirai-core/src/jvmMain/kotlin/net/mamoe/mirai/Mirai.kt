package net.mamoe.mirai

import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.core.Input
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.internal.InputStream

/**
 * Mirai 全局环境.
 */
actual object Mirai {
    actual var fileCacheStrategy: FileCacheStrategy
        get() = TODO("Not yet implemented")
        set(value) {}

    actual interface FileCacheStrategy {
        @MiraiExperimentalAPI
        actual fun newImageCache(input: Input): ExternalImage

        @MiraiExperimentalAPI
        actual fun newImageCache(input: ByteReadChannel): ExternalImage

        @MiraiExperimentalAPI
        actual fun newImageCache(input: InputStream): ExternalImage

        actual companion object Default : FileCacheStrategy {
            @MiraiExperimentalAPI
            actual override fun newImageCache(input: Input): ExternalImage {
                TODO("Not yet implemented")
            }

            @MiraiExperimentalAPI
            actual override fun newImageCache(input: ByteReadChannel): ExternalImage {
                TODO("Not yet implemented")
            }

            @MiraiExperimentalAPI
            actual override fun newImageCache(input: InputStream): ExternalImage {
                TODO("Not yet implemented")
            }
        }

    }

}