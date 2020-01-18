package net.mamoe.mirai.japt;

import kotlinx.io.core.ByteReadPacket;
import net.mamoe.mirai.BotAccount;
import net.mamoe.mirai.contact.GroupId;
import net.mamoe.mirai.contact.GroupInternalId;
import net.mamoe.mirai.data.AddFriendResult;
import net.mamoe.mirai.data.ImageLink;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.network.BotNetworkHandler;
import net.mamoe.mirai.utils.GroupNotFoundException;
import net.mamoe.mirai.utils.MiraiInternalAPI;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BlockingBot {
    /**
     * 账号信息
     */
    @MiraiInternalAPI
    @NotNull
    BotAccount getAccount();

    /**
     * QQ 号码. 实际类型为 uint
     */
    long getUin();

    /**
     * 日志记录器
     */
    @NotNull
    MiraiLogger getLogger();

    // region contacts

    /**
     * 与这个机器人相关的 QQ 列表. 机器人与 QQ 不一定是好友
     */
    @NotNull
    List<BlockingQQ> getQQs();

    /**
     * 获取缓存的 QQ 对象. 若没有对应的缓存, 则会线程安全地创建一个.
     */
    BlockingQQ getQQ(long id);

    /**
     * 与这个机器人相关的群列表. 机器人不一定是群成员.
     */
    @NotNull
    List<BlockingGroup> getGroups();

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 {@code id} 无效, 将会抛出 {@link GroupNotFoundException}
     */
    @NotNull
    BlockingGroup getGroup(long id);

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 {@code id} 无效, 将会抛出 {@link GroupNotFoundException}
     */
    @NotNull
    BlockingGroup getGroup(@NotNull GroupId id);

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 {@code internalId} 无效, 将会抛出 {@link GroupNotFoundException}
     */
    @NotNull
    BlockingGroup getGroup(@NotNull GroupInternalId internalId);

    // endregion

    // region network

    /**
     * 网络模块
     */
    @NotNull
    BotNetworkHandler getNetwork();

    /**
     * 登录.
     * <p>
     * 最终调用 [net.mamoe.mirai.network.BotNetworkHandler.login]
     *
     * @throws net.mamoe.mirai.utils.LoginFailedException
     */
    @SuppressWarnings("JavaDoc")
    void login();

    // endregion

    // region actions

    @NotNull
    ImageLink getLink(@NotNull Image image);

    byte[] downloadAsByteArray(@NotNull Image image);

    @NotNull
    ByteReadPacket download(@NotNull Image image);

    /**
     * 添加一个好友
     *
     * @param message 若需要验证请求时的验证消息.
     * @param remark  好友备注
     */
    @NotNull
    AddFriendResult addFriend(long id, @Nullable String message, @Nullable String remark);

    /**
     * 同意来自陌生人的加好友请求
     */
    void approveFriendAddRequest(long id, @Nullable String remark);
    // endregion

    /**
     * 关闭这个 [Bot], 停止一切相关活动. 不可重新登录.
     */
    void dispose(@Nullable Throwable throwable);
}
