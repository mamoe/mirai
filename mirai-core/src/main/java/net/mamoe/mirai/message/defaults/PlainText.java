package net.mamoe.mirai.message.defaults;

import net.mamoe.mirai.message.Message;

/**
 * @author Him188moe
 */
public final class PlainText extends Message {
    private final String text;

    public PlainText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
