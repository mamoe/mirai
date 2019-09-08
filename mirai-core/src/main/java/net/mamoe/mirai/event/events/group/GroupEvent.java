package net.mamoe.mirai.event.events.group;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.bot.BotEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Him188moe
 */
public abstract class GroupEvent extends BotEvent {
    private final Group group;

    public GroupEvent(Bot bot, Group group) {
        super(bot);
        this.group = group;
    }

    @NotNull
    public Group getGroup() {
        return group;
    }
}
