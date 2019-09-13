package net.mamoe.mirai.event.events.group;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.defaults.MessageChain;
import org.jetbrains.annotations.NotNull;

/**
 * @author Him188moe
 */
public final class GroupMessageEvent extends GroupEvent {
    public final QQ sender;
    public final MessageChain chain;
    public final String message;

    public GroupMessageEvent(@NotNull Bot bot, @NotNull Group group, @NotNull QQ sender, @NotNull MessageChain chain) {
        super(bot, group);
        this.sender = sender;
        this.chain = chain;
        this.message = chain.toString();
    }

    @NotNull
    public MessageChain getChain() {
        return chain;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @NotNull
    public QQ getSender() {
        return sender;
    }
}
