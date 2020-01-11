package net.mamoe.mirai.japt;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface BlockingContact {
    /**
     * 这个联系人所属 [Bot]
     */
    @NotNull
    Bot getBot();

    /**
     * 可以是 QQ 号码或者群号码 [GroupId].
     */
    long getId();

    /**
     * 向这个对象发送消息.
     */
    void sendMessage(@NotNull MessageChain messages);

    /**
     * 向这个对象发送消息.
     */
    void sendMessage(@NotNull String message);

    /**
     * 向这个对象发送消息.
     */
    void sendMessage(@NotNull Message message);
}
