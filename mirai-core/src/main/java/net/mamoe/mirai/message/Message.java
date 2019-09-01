package net.mamoe.mirai.message;

import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.defaults.At;
import net.mamoe.mirai.message.defaults.MessageChain;
import net.mamoe.mirai.message.defaults.PlainText;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

/**
 * @author Him188moe
 */
public abstract class Message {
    @Override
    public abstract String toString();

    /**
     * 把这个消息连接到另一个消息的头部. 相当于字符串相加
     * <p>
     * Connects this Message to the head of another Message.
     * That is, another message becomes the tail of this message.
     * This method does similar to {@link String#concat(String)}
     * <p>
     * E.g.:
     * PlainText a = new PlainText("Hello ");
     * PlainText b = new PlainText("world");
     * PlainText c = a.concat(b);
     * <p>
     * the text of c is "Hello world"
     *
     * @param tail tail
     * @return message connected
     */
    public Message concat(@NotNull Message tail) {
        return new MessageChain(this, Objects.requireNonNull(tail));
    }

    public Message concat(String tail) {
        return concat(new PlainText(tail));
    }


    public Message withImage(String imageId) {

        // TODO: 2019/9/1
        return this;
    }

    public Message withImage(BufferedImage image) {
        // TODO: 2019/9/1
        return this;

    }

    public Message withImage(File image) {
        // TODO: 2019/9/1
        return this;
    }

    public Message withAt(@NotNull QQ target) {
        this.concat(target.at());
        return this;
    }

    public Message withAt(int target) {
        this.concat(new At(target));
        return this;
    }
}
