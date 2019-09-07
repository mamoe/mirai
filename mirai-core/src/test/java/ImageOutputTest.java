import net.mamoe.mirai.utils.CharImageConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageOutputTest {
    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File((System.getProperty("user.dir") + "/VerificationCode.png").replace("//","/")));
        CharImageConverter charImageConvertor = new CharImageConverter(image,100);
        System.out.println(charImageConvertor.call());
    }
}
