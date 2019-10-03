package net.mamoe.mirai.utils

import java.awt.image.BufferedImage

/**
 * @author NaturalHG
 */
object CharImageUtil {

    @JvmOverloads
    fun createCharImg(image: BufferedImage, sizeWeight: Int = 100, sizeHeight: Int = 20): String {
        return CharImageConverter(image, sizeWeight).call()
    }

}