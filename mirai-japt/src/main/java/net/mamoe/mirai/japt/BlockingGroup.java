package net.mamoe.mirai.japt;

import net.mamoe.mirai.network.protocol.tim.packet.action.GroupInfo;

import java.util.Map;

@SuppressWarnings("unused")
public interface BlockingGroup extends BlockingContact {
    /**
     * 内部 ID. 内部 ID 为 [GroupId] 的映射
     */
    Long getInternalId();

    /**
     * 群主 (同步事件更新)
     * 进行 [updateGroupInfo] 时将会更新这个值.
     */
    BlockingMember getOwner();

    /**
     * 群名称 (同步事件更新)
     * 进行 [updateGroupInfo] 时将会更新这个值.
     */
    String getName();

    /**
     * 入群公告, 没有时为空字符串. (同步事件更新)
     * 进行 [updateGroupInfo] 时将会更新这个值.
     */
    String getAnnouncement();

    /**
     * 在 [Group] 实例创建的时候查询一次. 并与事件同步事件更新
     * <p>
     * **注意**: 获得的列表仅为这一时刻的成员列表的镜像. 它将不会被更新
     */
    Map<Long, BlockingMember> getMembers();

    /**
     * 获取群成员. 若此 ID 的成员不存在, 则会抛出 [kotlin.NoSuchElementException]
     */
    BlockingMember getMember(long id);

    /**
     * 更新群资料. 群资料会与服务器事件同步事件更新, 一般情况下不需要手动更新.
     *
     * @return 这一时刻的群资料
     */
    GroupInfo updateGroupInfo();

    /**
     * 让机器人退出这个群. 机器人必须为非群主才能退出. 否则将会失败
     */
    boolean quit();

    String toFullString();
}