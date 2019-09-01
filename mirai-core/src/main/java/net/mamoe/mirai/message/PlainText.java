package net.mamoe.mirai.message;

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
