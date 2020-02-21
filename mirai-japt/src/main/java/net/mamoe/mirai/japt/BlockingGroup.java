package net.mamoe.mirai.japt;

import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.data.MemberInfo;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiExperimentalAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public interface BlockingGroup extends BlockingContact {
    /**
     * 群名称.
     */
    @NotNull
    String getName();

    /**
     * 修改群名称
     * 频繁修改可能会被服务器拒绝.
     *
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     * @see MemberPermissionChangeEvent
     */
    void setName(@NotNull String name) throws PermissionDeniedException;

    /**
     * 入群公告, 没有时为空字符串. (同步事件更新)
     */
    @NotNull
    String getEntranceAnnouncement();

    /**
     * 修改入群公告.
     *
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     * @see GroupEntranceAnnouncementChangeEvent
     */
    void setEntranceAnnouncement(@NotNull String announcement) throws PermissionDeniedException;

    /**
     * 获取全员禁言状态
     *
     * @return 全员禁言状态. true 为开启
     */
    boolean isMuteAll();

    /**
     * 设置全体禁言
     *
     * @see GroupMuteAllEvent
     */
    void setMuteAll(boolean enabled) throws PermissionDeniedException;

    /**
     * 获取坦白说状态
     *
     * @return 坦白说状态, true 为允许
     */
    boolean isConfessTalkEnabled();

    /**
     * 设置坦白说状态
     *
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     * @see GroupAllowConfessTalkEvent
     */
    void setConfessTalk(boolean enabled) throws PermissionDeniedException;

    /**
     * 获取允许群员邀请好友入群的状态.
     *
     * @return 允许群员邀请好友入群的状态. `true` 为允许
     */
    boolean isAllowMemberInvite();

    /**
     * 设置允许群员邀请好友入群的状态.
     *
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     * @see GroupAllowMemberInviteEvent
     */
    void setAllowMemberInvite(boolean allow) throws PermissionDeniedException;

    /**
     * 获取自动加群审批的状态
     */
    boolean isAutoApproveEnabled();

    /**
     * 匿名聊天是否开启
     */
    boolean isAnonymousChatEnabled();

    /**
     * 同为 groupCode, 用户看到的群号码.
     */
    @Override
    long getId();

    /**
     * 群主 (同步事件更新)
     */
    @NotNull
    BlockingMember getOwner();

    /**
     * 机器人被禁言还剩余多少秒
     *
     * @see BotMuteEvent
     * @see GroupKt#isBotMuted
     */
    int getBotMuteRemaining();

    /**
     * 检查机器人是否正处于禁言状态
     */
    default boolean isBotMuted() {
        int time = getBotMuteRemaining();
        return time != 0 && time != 0xFFFFFFFF;
    }

    /**
     * 机器人在这个群里的权限
     *
     * @see BotGroupPermissionChangeEvent
     */
    @NotNull
    @MiraiExperimentalAPI
    MemberPermission getBotPermission();

    /**
     * 在 {@link Group} 实例创建的时候查询一次. 并与事件同步事件更新
     * <p>
     * **注意**: 获得的列表仅为这一时刻的成员列表的镜像. 它将不会被更新
     */
    @NotNull
    List<BlockingMember> getMembers();

    /**
     * 获取群成员. 若此 ID 的成员不存在, 则会抛出 {@link NoSuchElementException}
     */
    @NotNull
    BlockingMember getMember(long id);

    /**
     * 获取群成员. 若此 ID 的成员不存在则返回 null
     */
    @Nullable
    BlockingMember getMemberOrNull(long id);

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see MessageSendEvent.FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see MessageSendEvent.GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<Group> sendMessage(@NotNull MessageChain messages) throws EventCancelledException, IllegalStateException;

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see MessageSendEvent.FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see MessageSendEvent.GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<Group> sendMessage(@NotNull String message) throws EventCancelledException, IllegalStateException;

    /**
     * 向这个对象发送消息.
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException   发送群消息时若 [Bot] 被禁言抛出
     * @see MessageSendEvent.FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see MessageSendEvent.GroupMessageSendEvent  发送群消息事件. cancellable
     */
    MessageReceipt<Group> sendMessage(@NotNull Message message) throws EventCancelledException, IllegalStateException;

    /**
     * 检查此 id 的群成员是否存在
     */
    boolean containsMember(long id);

    /**
     * 让机器人退出这个群. 机器人必须为非群主才能退出. 否则将会失败
     */
    boolean quit();

    /**
     * 构造一个 [Member].
     * 非特殊情况请不要使用这个函数. 优先使用 [get].
     */
    @MiraiExperimentalAPI(message = "dangerous")
    @NotNull
    Member newMember(@NotNull MemberInfo memberInfo);

    @NotNull
    String toFullString();

    static long calculateGroupUinByGroupCode(long groupCode) {
        return Group.Companion.calculateGroupUinByGroupCode(groupCode);
    }

    static long calculateGroupCodeByGroupUin(long groupUin) {
        return Group.Companion.calculateGroupCodeByGroupUin(groupUin);
    }
}