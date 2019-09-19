package net.mamoe.mirai.event.events.qq;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.event.events.bot.BotEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Him188moe
 */
public abstract class FriendEvent extends BotEvent {
    public final QQ sender;

    public FriendEvent(@NotNull Bot bot, @NotNull QQ sender) {
        super(bot);
        this.sender = Objects.requireNonNull(sender);
    }

    @NotNull
    public QQ getSender() {
        return sender;
    }
}
