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
    private final QQ sender;
    private final MessageChain messageChain;
    private final String messageString;

    public GroupMessageEvent(@NotNull Bot bot, @NotNull Group group, @NotNull QQ sender, @NotNull MessageChain messageChain) {
        super(bot, group);
        this.sender = sender;
        this.messageChain = messageChain;
        this.messageString = messageChain.toString();
    }

    @NotNull
    public MessageChain getMessageChain() {
        return messageChain;
    }

    @NotNull
    public String getMessageString() {
        return messageString;
    }

    @NotNull
    public QQ getSender() {
        return sender;
    }
}
