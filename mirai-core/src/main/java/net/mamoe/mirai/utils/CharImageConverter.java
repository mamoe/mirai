package net.mamoe.mirai.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Convert IMAGE into Chars that could shows in terminal
 *
 * @author NaturalHG
 */
public final class CharImageConverter implements Callable<String> {

    /**
     * width should depends on the width of the terminal
     */
    private BufferedImage image;
    private int width;
    private double ignoreRate;

    public CharImageConverter(BufferedImage image, int width) {
        this(image, width, 0.95);
    }

    public CharImageConverter(BufferedImage image, int width, double ignoreRate) {
        this.image = image;
        this.width = width;
        this.ignoreRate = ignoreRate;
    }

    @Override
    public String call() {
        /*
         * resize Image
         * */
        int newHeight = (int) (this.image.getHeight() * (((double) width) / this.image.getWidth()));
        Image tmp = image.getScaledInstance(width, newHeight, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(width, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        this.image = dimg;

        int background = gray(image.getRGB(0, 0));

        StringBuilder builder = new StringBuilder();

        List<StringBuilder> lines = new ArrayList<>(this.image.getHeight());

        int minXPos = this.width;
        int maxXPos = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            StringBuilder builderLine = new StringBuilder();
            for (int x = 0; x < image.getWidth(); x++) {
                int gray = gray(image.getRGB(x, y));
                if (grayCompare(gray, background)) {
                    builderLine.append(" ");
                } else {
                    builderLine.append("#");
                    if (x < minXPos) {
                        minXPos = x;
                    }
                    if (x > maxXPos) {
                        maxXPos = x;
                    }
                }
            }
            if (builderLine.toString().isBlank()) {
                continue;
            }
            lines.add(builderLine);
        }
        for (StringBuilder line : lines) {
            builder.append(line.substring(minXPos, maxXPos)).append("\n");
        }
        return builder.toString();
    }

    private static int gray(int rgb) {
        int R = (rgb & 0xff0000) >> 16;
        int G = (rgb & 0x00ff00) >> 8;
        int B = rgb & 0x0000ff;
        return (R * 30 + G * 59 + B * 11 + 50) / 100;
    }

    public boolean grayCompare(int g1, int g2) {
        return ((double) Math.min(g1, g2) / Math.max(g1, g2)) >= ignoreRate;
    }

}
