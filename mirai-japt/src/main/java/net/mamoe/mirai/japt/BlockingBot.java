package net.mamoe.mirai.japt;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotAccount;
import net.mamoe.mirai.BotFactoryJvmKt;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.data.AddFriendResult;
import net.mamoe.mirai.data.GroupInfo;
import net.mamoe.mirai.data.MemberInfo;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.network.BotNetworkHandler;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.MiraiExperimentalAPI;
import net.mamoe.mirai.utils.MiraiInternalAPI;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * 对 {@link Bot} 的阻塞式包装
 *
 * @see Bot
 */
@SuppressWarnings("unused")
public interface BlockingBot {
    /**
     * 使用默认配置创建一个机器人实例
     *
     * @param id       qq 号
     * @param password 密码
     * @return 机器人实例
     */
    static BlockingBot newInstance(long id, String password) {
        return BlockingContacts.createBlocking(BotFactoryJvmKt.Bot(id, password));
    }

    /**
     * 使用特定配置创建一个机器人实例
     *
     * @param id       qq 号
     * @param password 密码
     * @return 机器人实例
     */
    static BlockingBot newInstance(long id, String password, BotConfiguration configuration) {
        return BlockingContacts.createBlocking(BotFactoryJvmKt.Bot(id, password, configuration));
    }

    // 要获取 Bot 实例列表, 请前往 BotKt

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
     * 获取昵称
     */
    @NotNull
    @MiraiExperimentalAPI(message = "还未支持")
    String getNick();

    /**
     * 日志记录器
     */
    @NotNull
    MiraiLogger getLogger();

    // region contacts

    /**
     * 获取自身 QQ 实例
     */
    @NotNull
    QQ getSelfQQ();

    /**
     * 与这个机器人相关的 QQ 列表. 机器人与 QQ 不一定是好友
     */
    @NotNull
    List<BlockingQQ> getFriendList();

    /**
     * 获取缓存的 QQ 对象. 若没有对应的缓存, 则会线程安全地创建一个.
     */
    @NotNull
    BlockingQQ getFriend(long id);

    /**
     * 与这个机器人相关的群列表. 机器人不一定是群成员.
     */
    @NotNull
    List<BlockingGroup> getGroupList();

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 {@code id} 无效, 将会抛出 {@link NoSuchElementException}
     */
    @NotNull
    BlockingGroup getGroup(long id);


    // endregion

    // region network

    /**
     * 网络模块
     */
    @NotNull
    BotNetworkHandler getNetwork();

    /**
     * 登录.
     */
    void login();

    /**
     * 查询群列表. 返回值前 32 bits 为 uin, 后 32 bits 为 groupCode
     */
    @NotNull
    Stream<Long> queryGroupList();

    /**
     * 查询群资料. 获得的仅为当前时刻的资料.
     * 请优先使用 {@link #getGroup(long)} 然后查看群资料.
     */
    @NotNull
    GroupInfo queryGroupInfo(long groupCode);

    /**
     * 查询群成员列表.
     * 请优先使用 {@link #getGroup(long)} , {@link BlockingGroup#getMembers()} 查看群成员.
     * <p>
     * 这个函数很慢. 请不要频繁使用.
     */
    @NotNull
    Stream<MemberInfo> queryGroupMemberList(long groupUin, long groupCode, long ownerId);

    // endregion

    // region actions

    /**
     * 下载图片到 {@code outputStream}.
     * 不会自动关闭 {@code outputStream}
     */
    void downloadTo(@NotNull Image image, @NotNull OutputStream outputStream);

    /**
     * 下载图片到 {@code outputStream} 并关闭 stream
     */
    void downloadAndClose(@NotNull Image image, @NotNull OutputStream outputStream);

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
    void close(@Nullable Throwable throwable);

    /**
     * @deprecated 使用 {@link #close(Throwable)}
     */
    @Deprecated
    default void dispose(@Nullable Throwable throwable) {
        close(throwable);
    }
}
