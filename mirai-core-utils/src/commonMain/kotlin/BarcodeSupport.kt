/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils


public expect class PlatformImage

public interface BarcodeSupport {
    public val available: Boolean

    /**
     * @return `null` --- Unsupported
     */
    public fun generateQRCode(
        content: String,
        width: Int,
        height: Int
    ): PlatformImage?

    public companion object INSTANCE : BarcodeSupport by initService()
}

private fun initService(): BarcodeSupport {
    return loadServiceOrNull(
        BarcodeSupport::class,
        "net.mamoe.mirai.utils.barcode.BarcodeSupportImpl"
    ) ?: DummyBSI
}

private object DummyBSI : BarcodeSupport {
    override val available: Boolean get() = false
    override fun generateQRCode(content: String, width: Int, height: Int): PlatformImage? = null
}