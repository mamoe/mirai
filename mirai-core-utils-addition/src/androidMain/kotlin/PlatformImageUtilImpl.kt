/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.addition

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import net.mamoe.mirai.utils.PlatformImage
import net.mamoe.mirai.utils.PlatformImageUtil

public class PlatformImageUtilImpl : PlatformImageUtil {
    override val available: Boolean get() = true

    override fun generateQRCode(content: String, width: Int, height: Int): PlatformImage? {
        val bitMatrix: BitMatrix = try {
            QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
        } catch (e: WriterException) {
            throw RuntimeException(e)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y, if (bitMatrix[x, y]) {
                        0x000000
                    } else {
                        0xFFFFFF
                    }
                )
            }
        }
        return bitmap
    }
}