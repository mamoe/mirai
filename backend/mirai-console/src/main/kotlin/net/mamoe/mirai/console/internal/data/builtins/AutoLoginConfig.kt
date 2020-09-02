package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

internal object AutoLoginConfig : AutoSavePluginConfig() {
    override val saveName: String
        get() = "AutoLogin"

    @ValueDescription(
        """
        账号和明文密码列表
    """
    )
    val plainPasswords: MutableMap<Long, String> by value(mutableMapOf())


    @ValueDescription(
        """
        账号和 MD5 密码列表
    """
    )
    val md5Passwords: MutableMap<Long, String> by value(mutableMapOf())
}