package net.mamoe.mirai.japt;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.event.events.BeforeImageUploadEvent;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.ImageUploadEvent;
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent;
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.ExternalImage;
import org.jetbrains.annotations.NotNull;

/**
 * 对 {@link Contact} 的阻塞式包装.
 */
@SuppressWarnings("unused")
public interface BlockingContact {
    /**
     * 这个联系人所属 {@link Bot}
     */
    @NotNull
    BlockingBot getBot();

    /**
     * 可以是 QQ 号码或者群号码.
     * <p>
     * 对于 QQ, {@code uin} 与 {@code id} 是相同的意思.
     * 对于 Group, {@code groupCode} 与 {@code id} 是相同的意思.
     */
    long getId();

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<? extends Contact> sendMessage(@NotNull MessageChain messages) throws EventCancelledException, IllegalStateException;

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<? extends Contact> sendMessage(@NotNull String message) throws EventCancelledException, IllegalStateException;

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<? extends Contact> sendMessage(@NotNull Message message) throws EventCancelledException, IllegalStateException;

    /**
     * 上传一个图片以备发送.
     * 群图片与好友图片在服务器上是通用的, 在 mirai 目前不通用.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @see BeforeImageUploadEvent 图片发送前事件, cancellable
     * @see ImageUploadEvent 图片发送完成事件
     */
    Image uploadImage(@NotNull ExternalImage image) throws EventCancelledException;

    /**
     * 判断 {@code this} 和 {@code other} 是否是相同的类型, 并且 {@link Contact#getId()} 相同.
     * <p>
     * 注:
     * {@link Contact#getId()} 相同的 {@link Member} 和 {@link QQ}, 他们并不 equals.
     * 因为, {@link Member} 含义为群员, 必属于一个群.
     * 而 {@link QQ} 含义为一个独立的人, 可以是好友, 也可以是陌生人.
     */
    boolean equals(Object other);
}
