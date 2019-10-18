package net.mamoe.mirai.utils

import kotlin.jvm.JvmOverloads


expect class PlatformImage

@JvmOverloads
expect fun PlatformImage.toByteArray(formatName: String = "JPG"): ByteArray