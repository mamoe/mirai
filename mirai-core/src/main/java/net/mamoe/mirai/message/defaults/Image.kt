package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import javax.imageio.ImageIO

/**
 * @author Him188moe
 */
class Image : Message {
    override val type: Int = MessageId.IMAGE

    private var imageID: String? = null

    constructor(inputStream: InputStream) {

    }

    constructor(image: BufferedImage) {

    }

    @Throws(FileNotFoundException::class)
    constructor(imageFile: File) : this(FileInputStream(imageFile)) {
    }

    @Throws(IOException::class)
    constructor(url: URL) : this(ImageIO.read(url)) {
    }

    /**
     * {xxxxx}.jpg
     *
     * @param imageID
     */
    constructor(imageID: String) {
        this.imageID = imageID
    }

    override fun toStringImpl(): String {
        return imageID!!
    }

    override fun toByteArray(): ByteArray {
        TODO()
    }

    override fun valueEquals(another: Message): Boolean {
        if (another !is Image) {
            return false
        }
        return this.imageID == another.imageID
    }
}