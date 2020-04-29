package net.mamoe.mirai.utils.internal

import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.io.core.Input
import net.mamoe.mirai.utils.ExternalImage
import java.io.InputStream

internal actual fun ByteReadChannel.asReusableInput(): ExternalImage.ReusableInput {
    TODO("Not yet implemented")
}

internal actual fun Input.asReusableInput(): ExternalImage.ReusableInput {
    TODO("Not yet implemented")
}

internal actual fun InputStream.asReusableInput(): ExternalImage.ReusableInput {
    TODO("Not yet implemented")
}