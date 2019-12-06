package net.mamoe.mirai.japt;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.MessageChain;
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
     * <p>
     * 速度太快会被服务器屏蔽(无响应). 在测试中不延迟地发送 6 条消息就会被屏蔽之后的数据包 1 秒左右.
     */
    void sendMessage(@NotNull MessageChain messages);
}
