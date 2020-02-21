package net.mamoe.mirai.japt;

import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.data.FriendNameRemark;
import net.mamoe.mirai.data.PreviousNameList;
import net.mamoe.mirai.data.Profile;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.MessageSendEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiExperimentalAPI;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface BlockingQQ extends BlockingContact {
    /**
     * 获取 QQ 号码
     *
     * @return QQ 号码
     */
    @Override
    long getId();

    /**
     * 获取昵称
     *
     * @return 昵称
     */
    String getNick();

    /**
     * 查询用户资料
     */
    @MiraiExperimentalAPI(message = "还未支持")
    @NotNull
    Profile queryProfile();

    /**
     * 查询曾用名.
     * <p>
     * 曾用名可能是:
     * - 昵称
     * - 共同群内的群名片
     */
    @MiraiExperimentalAPI(message = "还未支持")
    @NotNull
    PreviousNameList queryPreviousNameList();

    /**
     * 查询机器人账号给这个人设置的备注
     */
    @MiraiExperimentalAPI(message = "还未支持")
    @NotNull
    FriendNameRemark queryRemark();

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see MessageSendEvent.FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see MessageSendEvent.GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<QQ> sendMessage(@NotNull MessageChain messages) throws EventCancelledException, IllegalStateException;

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see MessageSendEvent.FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see MessageSendEvent.GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<QQ> sendMessage(@NotNull String message) throws EventCancelledException, IllegalStateException;

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see MessageSendEvent.FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see MessageSendEvent.GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<QQ> sendMessage(@NotNull Message message) throws EventCancelledException, IllegalStateException;

}
