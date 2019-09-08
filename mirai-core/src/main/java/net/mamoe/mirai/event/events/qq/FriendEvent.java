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
    private final QQ qq;

    public FriendEvent(@NotNull Bot bot, @NotNull QQ qq) {
        super(bot);
        this.qq = Objects.requireNonNull(qq);
    }

    @NotNull
    public QQ getQQ() {
        return qq;
    }
}
