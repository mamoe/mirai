package net.mamoe.mirai.japt;

import kotlin.text.StringsKt;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.PermissionDeniedException;
import net.mamoe.mirai.event.events.MemberCardChangeEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface BlockingMember extends BlockingQQ {
    /**
     * 所在的群
     */
    @NotNull
    BlockingGroup getGroup();

    /**
     * 权限
     */
    @NotNull
    MemberPermission getPermission();

    /**
     * 群名片. 可能为空.
     */
    @NotNull
    String getNameCard();

    /**
     * 修改群名片. 将会触发事件
     *
     * @throws PermissionDeniedException 无权限修改时
     * @see #getGroupCardOrNick() 获取非空群名片或昵称
     * @see MemberCardChangeEvent 群名片被管理员, 自己或 [Bot] 改动事件
     */
    void setNameCard(@NotNull String nameCard) throws PermissionDeniedException;

    /**
     * 获取群名片或昵称
     */
    @NotNull
    default String getGroupCardOrNick() {
        String nameCard = this.getNameCard();
        if (!StringsKt.isBlank(nameCard)) {
            return nameCard;
        }
        return this.getNick();
    }

    /**
     * 禁言
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @throws PermissionDeniedException 无权限修改时
     */
    void mute(int durationSeconds);

    /**
     * 禁言
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @throws PermissionDeniedException 无权限修改时
     */
    default void mute(long durationSeconds) {
        mute((int) durationSeconds);
    }

    /**
     * 解除禁言
     *
     * @throws PermissionDeniedException 无权限修改时
     */
    void unmute();

    /**
     * 踢出该成员.
     *
     * @param message 消息
     * @throws PermissionDeniedException 无权限修改时
     */
    void kick(@NotNull String message);

    /**
     * 踢出该成员.
     *
     * @throws PermissionDeniedException 无权限修改时
     */
    default void kick() {
        kick("");
    }
}
