package net.mamoe.mirai.event.events.qq;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.defaults.MessageChain;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Him188moe
 */
public final class FriendMessageEvent extends FriendEvent {
    private final MessageChain messageChain;

    public FriendMessageEvent(@NotNull Robot robot, @NotNull QQ sender, @NotNull MessageChain messageChain) {
        super(robot, sender);
        this.messageChain = Objects.requireNonNull(messageChain);
    }

    @NotNull
    public MessageChain message() {
        return messageChain;
    }
}
