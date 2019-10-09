package net.mamoe.mirai.utils

import java.awt.image.BufferedImage

/**
 * @author NaturalHG
 */
@JvmOverloads
fun BufferedImage.createCharImg(sizeWeight: Int = 100): String {
    return CharImageConverter(this, sizeWeight).call()
}