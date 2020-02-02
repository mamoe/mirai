package net.mamoe.mirai.japt;

import net.mamoe.mirai.contact.Group;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public interface BlockingGroup extends BlockingContact {
    /**
     * 群主 (同步事件更新)
     */
    @NotNull
    BlockingMember getOwner();

    /**
     * 群名称 (同步事件更新)
     */
    @NotNull
    String getName();

    /**
     * 入群公告, 没有时为空字符串. (同步事件更新)
     */
    @NotNull
    String getAnnouncement();

    /**
     * 在 {@link Group} 实例创建的时候查询一次. 并与事件同步事件更新
     * <p>
     * **注意**: 获得的列表仅为这一时刻的成员列表的镜像. 它将不会被更新
     */
    @NotNull
    Map<Long, BlockingMember> getMembers();

    /**
     * 获取群成员. 若此 ID 的成员不存在, 则会抛出 {@link NoSuchElementException}
     */
    @NotNull
    BlockingMember getMember(long id);

    /**
     * 让机器人退出这个群. 机器人必须为非群主才能退出. 否则将会失败
     */
    boolean quit();

    @NotNull
    String toFullString();
}