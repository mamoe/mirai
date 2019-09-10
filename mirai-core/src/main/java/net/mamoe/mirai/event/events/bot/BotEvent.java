package net.mamoe.mirai.event.events.bot;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.MiraiEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class BotEvent extends MiraiEvent {
    private final Bot bot;

    public BotEvent(@NotNull Bot bot) {
        this.bot = Objects.requireNonNull(bot);
    }

    @NotNull
    public Bot getBot() {
        return bot;
    }

}
