package net.mamoe.mirai.utils;
import java.awt.image.BufferedImage;

public final class CharImageUtil {

    public static String createCharImg(BufferedImage image) {
        return createCharImg(image, 100, 20);
    }

    public static String createCharImg(BufferedImage image, int sizeWeight, int sizeHeight) {
        return new CharImageConverter(image,sizeWeight).call();
    }

}