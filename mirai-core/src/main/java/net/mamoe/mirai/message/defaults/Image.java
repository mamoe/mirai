package net.mamoe.mirai.message.defaults;

import net.mamoe.mirai.message.Message;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

/**
 * @author Him188moe
 */
public final class Image extends Message {
    public Image(InputStream inputStream) {

    }

    public Image(BufferedImage image) {

    }

    public Image(File imageFile) throws FileNotFoundException {
        this(new FileInputStream(imageFile));
    }

    public Image(URL url) throws IOException {
        this(ImageIO.read(url));
    }

    /**
     * {xxxxx}.jpg
     *
     * @param imageID
     */
    public Image(String imageID) {

    }

    @Override
    public String toString() {
        return null;
    }
}
