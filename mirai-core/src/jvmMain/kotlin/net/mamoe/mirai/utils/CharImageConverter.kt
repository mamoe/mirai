package net.mamoe.mirai.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.Callable
import kotlin.math.max
import kotlin.math.min

/**
 * Convert IMAGE into Chars that could shows in terminal
 *
 * @author NaturalHG
 */
class CharImageConverter @JvmOverloads constructor(
        /**
         * width should depends on the width of the terminal
         */
        private var image: BufferedImage?, private val width: Int, private val ignoreRate: Double = 0.95) : Callable<String> {

    override fun call(): String {
        /*
         * resize Image
         * */
        val newHeight = (this.image!!.height * (width.toDouble() / this.image!!.width)).toInt()
        val tmp = image!!.getScaledInstance(width, newHeight, Image.SCALE_SMOOTH)
        val dimg = BufferedImage(width, newHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = dimg.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        this.image = dimg

        val background = gray(image!!.getRGB(0, 0))

        val builder = StringBuilder()

        val lines = ArrayList<StringBuilder>(this.image!!.height)

        var minXPos = this.width
        var maxXPos = 0

        for (y in 0 until image!!.height) {
            val builderLine = StringBuilder()
            for (x in 0 until image!!.width) {
                val gray = gray(image!!.getRGB(x, y))
                if (grayCompare(gray, background)) {
                    builderLine.append(" ")
                } else {
                    builderLine.append("#")
                    if (x < minXPos) {
                        minXPos = x
                    }
                    if (x > maxXPos) {
                        maxXPos = x
                    }
                }
            }
            if (builderLine.toString().isBlank()) {
                continue
            }
            lines.add(builderLine)
        }
        for (line in lines) {
            builder.append(line.substring(minXPos, maxXPos)).append("\n")
        }
        return builder.toString()
    }

    private fun gray(rgb: Int): Int {
        val R = rgb and 0xff0000 shr 16
        val G = rgb and 0x00ff00 shr 8
        val B = rgb and 0x0000ff
        return (R * 30 + G * 59 + B * 11 + 50) / 100
    }

    fun grayCompare(g1: Int, g2: Int): Boolean {
        return min(g1, g2).toDouble() / max(g1, g2) >= ignoreRate
    }

}
