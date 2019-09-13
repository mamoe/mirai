package net.mamoe.mirai.event.events.qq;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.defaults.MessageChain;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Him188moe
 */
public final class FriendMessageEvent extends FriendEvent {
    public final MessageChain message;

    public FriendMessageEvent(@NotNull Bot bot, @NotNull QQ sender, @NotNull MessageChain message) {
        super(bot, sender);
        this.message = Objects.requireNonNull(message);
    }

    @NotNull
    public MessageChain message() {
        return message;
    }
}
