package net.mamoe.mirai.japt;

import net.mamoe.mirai.contact.MemberPermission;

@SuppressWarnings("unused")
public interface BlockingMember {
    /**
     * 所在的群
     */
    BlockingGroup getGroup();

    /**
     * 权限
     */
    MemberPermission getPermission();

    /**
     * 禁言
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 若机器人无权限禁言这个群成员, 返回 `false`
     */
    Boolean mute(int durationSeconds);

    /**
     * 解除禁言
     */
    void unmute();
}
