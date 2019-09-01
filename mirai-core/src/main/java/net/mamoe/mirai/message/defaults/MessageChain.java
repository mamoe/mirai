package net.mamoe.mirai.message.defaults;

import net.mamoe.mirai.message.Message;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Him188moe
 */
public final class MessageChain extends Message {
    private LinkedList<Message> list = new LinkedList<>();

    public MessageChain(@NotNull Message head, @NotNull Message tail) {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);

        list.add(head);
        list.add(tail);
    }

    @Override
    public synchronized String toString() {
        return this.list.stream().map(Message::toString).collect(Collectors.joining(""));
    }

    @Override
    public synchronized Message concat(@NotNull Message tail) {
        this.list.add(tail);
        return this;
    }
}
