package net.mamoe.mirai.data

public interface StrangerInfo : UserInfo {
    /**
     * 陌生人的QQ号码
     */
    public override val id: Long

    /**
     * 陌生人的昵称
     *
     */
    public override val nick: String

    /**
     * 陌生人来源的群
     *
     * 当不是来源于群时为0
     */
    public val fromGroup: Long
}