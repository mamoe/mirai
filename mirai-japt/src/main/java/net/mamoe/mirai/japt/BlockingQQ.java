package net.mamoe.mirai.japt;

import net.mamoe.mirai.data.FriendNameRemark;
import net.mamoe.mirai.data.PreviousNameList;
import net.mamoe.mirai.data.Profile;
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
}
