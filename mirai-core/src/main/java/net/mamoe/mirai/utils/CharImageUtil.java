package net.mamoe.mirai.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片转字符图片, 来自 CSDN 开源
 *
 * @author zhoujie https://blog.csdn.net/qq_37902949/article/details/81228566
 */
public final class CharImageUtil {

    public static String createCharImg(BufferedImage image) {
        return createCharImg(image, 100, 20);
    }

    public static String createCharImg(BufferedImage image, int sizeWeight, int sizeHeight) {
        //生成字符图片
        image = resize(image, sizeWeight, sizeHeight);
        int width = image.getWidth();
        int height = image.getHeight();

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < height; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);
                int R = (rgb & 0xff0000) >> 16;
                int G = (rgb & 0x00ff00) >> 8;
                int B = rgb & 0x0000ff;
                int gray = (R * 30 + G * 59 + B * 11 + 50) / 100;
                int index = 31 * gray / 255;
                line.append(asc[index]); //添加每个字符
            }
            output.append(line).append("\n");
        }
        return output.toString();
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private final static char[] asc = {' ', '`', '.', '^', ',', ':', '~', '"',
            '<', '!', 'c', 't', '+', '{', 'i', '7', '?', 'u', '3', '0', 'p', 'w',
            '4', 'A', '8', 'D', 'X', '%', '#', 'H', 'W', 'M'};

}