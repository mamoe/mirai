package net.mamoe.mirai.japt;

import net.mamoe.mirai.data.FriendNameRemark;
import net.mamoe.mirai.data.PreviousNameList;
import net.mamoe.mirai.data.Profile;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface BlockingQQ extends BlockingContact {
    /**
     * 查询用户资料
     */
    @NotNull
    Profile queryProfile();

    /**
     * 查询曾用名.
     * <p>
     * 曾用名可能是:
     * - 昵称
     * - 共同群内的群名片
     */
    @NotNull
    PreviousNameList queryPreviousNameList();

    /**
     * 查询机器人账号给这个人设置的备注
     */
    @NotNull
    FriendNameRemark queryRemark();
}
