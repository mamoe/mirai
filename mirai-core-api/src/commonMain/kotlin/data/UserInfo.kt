package net.mamoe.mirai.data

import net.mamoe.mirai.LowLevelApi

@LowLevelApi
public interface UserInfo {
    public val uin: Long

    public val nick: String

    public val remark: String
}
