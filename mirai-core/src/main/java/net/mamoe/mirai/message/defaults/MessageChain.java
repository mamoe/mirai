package net.mamoe.mirai.message.defaults;

import net.mamoe.mirai.message.Message;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public MessageChain(@NotNull Message message) {
        Objects.requireNonNull(message);
        list.add(message);
    }

    public List<Message> toList() {
        return List.copyOf(list);
    }

    public Stream<Message> stream() {
        return new ArrayList<>(list).stream();
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
